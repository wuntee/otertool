package com.wuntee.oter.adb;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.command.BackgroundCommand;
import com.wuntee.oter.exception.CommandExecutionException;

public class BackgroundAdbCommand extends BackgroundCommand {
	private static Logger logger = Logger.getLogger(BackgroundAdbCommand.class);
	
	public BackgroundAdbCommand(String command) {
		super(new String[]{OterStatics.getAdbExecutable(), command});
	}

	public BackgroundAdbCommand(String command[]) {
		super(command);
		
		String[] newCommand = new String[command.length+1];
		newCommand[0] = OterStatics.getAdbExecutable();
		for(int i=0; i<command.length; i++){
			newCommand[i+1] = command[i];
		}
		this.command = newCommand;
	}
	
	/* (non-Javadoc)
	 * @see com.wuntee.aat.command.BackgroundCommand#execute()
	 * Throws: IOException
	 * Throws: CommandExecutionException
	 */
	public int execute() throws Exception{
		logger.debug("Executing");
		if(!AdbWorkshop.isConnected()){
			throw new CommandExecutionException("There is no Android device connected, or ADB is not running.");
		}
		
		return(super.execute());
	}

}
