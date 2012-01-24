package com.wuntee.oter.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

public class BackgroundCommand extends AbstractCommand{
	private static Logger logger = Logger.getLogger(BackgroundCommand.class);
	
	private boolean running = false;
	protected BufferedReader stdout;
	protected BufferedReader stderr;
	protected BufferedWriter stdin;

	public BackgroundCommand(String[] command) {
		super(command);
	}

	public BackgroundCommand(String command) {
		super(command);
	}
	
	@Override
	public int execute() throws Exception {
		logger.debug("Execute");
		String fullCommand = "";
		for(int i=0; i<this.command.length; i++)
			fullCommand = fullCommand + " " + command[i];
		logger.debug("Executing background: " + fullCommand);
			
		this.running = true;
		this.child = Runtime.getRuntime().exec(this.command);
		
		this.stdout = new BufferedReader(new InputStreamReader(this.child.getInputStream()));
		this.stderr = new BufferedReader(new InputStreamReader(this.child.getErrorStream()));
		this.stdin = new BufferedWriter(new OutputStreamWriter(this.child.getOutputStream()));
		
		logger.debug("Returning from executing background command.");
		return(0);
	}
	
	public void close() {
		this.running = false;
		this.child.destroy();
	}
	
	public BufferedReader getStdout(){
		return(stdout);
	}
	
	public BufferedReader getStderr(){
		return(stderr);
	}
	
	@Override
	public void checkError() {
		// TODO Auto-generated method stub
		
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public BufferedWriter getStdin() {
		return stdin;
	}
	
	

}
