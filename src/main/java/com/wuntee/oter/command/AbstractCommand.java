package com.wuntee.oter.command;

import org.apache.log4j.Logger;

public abstract class AbstractCommand {
	private static Logger logger = Logger.getLogger(AbstractCommand.class);
	
	protected String[] command;
	protected Process child;

	public AbstractCommand(String[] command){
		String fullCommand = "";
		for(int i=0; i<command.length; i++)
			fullCommand = fullCommand + " " + command[i];
		logger.info("Setting command:" + fullCommand);
		
		this.command = command;
	}
	
	public AbstractCommand(String command){
		this(new String[]{command});
	}

	public abstract int execute() throws Exception;
	public abstract void checkError();

	public String[] getCommand() {
		return command;
	}

	public void setCommand(String[] command) {
		this.command = command;
	}
	
}
