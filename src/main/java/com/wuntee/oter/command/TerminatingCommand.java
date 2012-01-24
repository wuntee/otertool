package com.wuntee.oter.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.wuntee.oter.exception.CommandFailedException;

public class TerminatingCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(TerminatingCommand.class);
	
	protected List<String> output;
	protected List<String> error;
	private int returnCode;

	public TerminatingCommand(String[] command){
		super(command);
		output = new LinkedList<String>();
		error = new LinkedList<String>();
	}
	
	public TerminatingCommand(String command){
		this(new String[]{command});
	}
	
	public int execute() throws IOException, InterruptedException, CommandFailedException {
		this.child = Runtime.getRuntime().exec(this.command);

		BufferedReader b = new BufferedReader(new InputStreamReader(this.child.getErrorStream()));
		
		String l;
		while( (l = b.readLine()) != null){
			logger.debug("STDERR: " + l);
			error.add(l);
		}
		b = new BufferedReader(new InputStreamReader(this.child.getInputStream()));
		while( (l = b.readLine()) != null){
			logger.debug("STDOUT: " + l);;
			output.add(l);
		}
		
		this.returnCode = this.child.waitFor();
		logger.debug("Return code: " + this.returnCode);
		
 		if(this.returnCode != 0){
 			List<String> ex = new LinkedList<String>();
 			ex.addAll(output);
 			ex.addAll(error);
			throw new CommandFailedException(this.command, ex, this.returnCode);
		}
		
		return(this.returnCode);
	}
	
	public int executeNoErrorMonitor() throws CommandFailedException, InterruptedException, IOException {
		this.child = Runtime.getRuntime().exec(this.command);

		BufferedReader b = new BufferedReader(new InputStreamReader(this.child.getInputStream()));
		String l;
		while( (l = b.readLine()) != null){
			logger.debug("STDOUT: " + l);;
			output.add(l);
		}
		
		this.returnCode = this.child.waitFor();
		logger.debug("Return code: " + this.returnCode);
		
 		if(this.returnCode != 0){
 			List<String> ex = new LinkedList<String>();
 			ex.addAll(output);
 			ex.addAll(error);
			throw new CommandFailedException(this.command, ex, this.returnCode);
		}
		
		return(this.returnCode);
	}
	
	public void checkError(){
		
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	public List<String> getOutput() {
		return output;
	}

	public void setOutput(List<String> output) {
		this.output = output;
	}

	public List<String> getError() {
		return error;
	}

	public void setError(List<String> error) {
		this.error = error;
	}
	
	
}
