package com.wuntee.oter.adb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.exception.CommandFailedException;

public class AdbShell extends BackgroundAdbCommand {
	public static void main(String[] args) throws Exception{
		AdbShell s = new AdbShell();
		s.start();
		s.getRootShell();
	}
	
	private static Logger logger = Logger.getLogger(AdbShell.class);
	
	private String prompt;
	
	public AdbShell() {
		super("shell");
	}
	
	public boolean isRootShell(){
		return(prompt.equals(OterStatics.ADB_ROOT_SHELL));
	}
	
	public int start() throws Exception{
		return(execute());
	}
	
	public int execute() throws Exception{
		super.execute();

		waitForNewPrompt();
		
		return(0);
	}
	
	private List<String> waitForNewPrompt() throws IOException{
		// Gallaxy shell = "shell@android:/ $ "
		List<String> ret = new LinkedList<String>();

		String line = "";
		while(!isPrompt(line)){
			char c = (char)this.stdout.read();
			if(c == '\r' || c=='\n'){
				if(!line.trim().equals("")){
					logger.debug("Line: " + line);
					ret.add(line);
				}
				line = "";
			} else {
				line = line + c;
			}
		}
		//ret = ret.subList(1, ret.size());
		
		this.prompt = line;
		logger.debug("Prompt: '" + line + "'");
		
		return(ret);
	}
	
	private boolean isPrompt(String line){
		for(String prompt : OterStatics.ADB_SHELLS){
			if(line.equals(prompt)){
				return(true);
			}
		}
		return(false);
	}
	
	public List<String> sendCommand(String command) throws IOException, CommandFailedException {
		List<String> ret = _sendCommand(command);
		
		// Check for return code
		List<String> code = _sendCommand("echo $?");
		if(!code.get(0).equals("0")){
			throw new CommandFailedException(command, ret, code.get(0));
		}
		
		return(ret);
	}
	
	private List<String> _sendCommand(String command) throws IOException{
		logger.debug("Sending command: " + command);
		List<String> ret = new LinkedList<String>();
		
		this.stdin.write(command + "\n");
		this.stdin.flush();
		
		// Monitor until a prompt
		logger.debug("Waiting for prompt.");
		String line = "";
		while(!line.equals(prompt)){
			char c = (char)this.stdout.read();
			if(c == '\r' || c=='\n'){
				if(!line.trim().equals("")){
					logger.debug("Line: " + line);
					ret.add(line);
				}
				line = "";
			} else {
				line = line + c;
			}
		}
		ret = ret.subList(1, ret.size());
		logger.debug("Got prompt, returning: " + ret);
		return(ret);
	}
	
	public void getRootShell() throws IOException, CommandFailedException{
		if(!isRootShell()){
			logger.debug("Sending su");

			this.stdin.write("su\n");
			this.stdin.flush();
			
			// Monitor until a prompt
			logger.debug("Waiting for prompt or root prompt.");
			List<String> line = waitForNewPrompt();

			if(!this.isRootShell()){
				// Did not get root prompt
				throw new CommandFailedException("su", line, "-1");
			}
			logger.debug("Got root prompt, returning");
		} else {
			logger.debug("Already root, returning");
		}
	}
	
}
