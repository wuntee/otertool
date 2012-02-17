package com.wuntee.oter.adb;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.command.TerminatingCommand;

public class TerminatingAdbCommand extends TerminatingCommand {
	private static Logger logger = Logger.getLogger(TerminatingAdbCommand.class);
	
	public TerminatingAdbCommand(String[] command) {
		super(getAdbArray(command));
	}
	
	private static String[] getAdbArray(String[] command){
		List<String> cmd = new LinkedList<String>();
		
		cmd.add(OterStatics.getAdbExecutable());
		
		String device = OterWorkshop.getProperty(OterStatics.PROPERTY_DEVICE);
		if(device != null){
			cmd.add("-s");
			cmd.add(device);
		}
		
		for(String a : command){
			cmd.add(a);
		}
		
		logger.debug(cmd);
		
		return(cmd.toArray(command));	
	}

}
