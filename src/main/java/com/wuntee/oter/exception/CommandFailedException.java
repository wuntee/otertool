package com.wuntee.oter.exception;

import java.util.List;

public class CommandFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public CommandFailedException(String command, List<String> output, String retCode){
		super(formatError(command, output, retCode));
	}
	
	public CommandFailedException(String command, String output, String retCode){
		super(formatError(command, output, retCode));
	}
	
	public CommandFailedException(String[] command, List<String> output, int returnCode) {
		super(formatError(command, output, Integer.toString(returnCode)));
	}

	public CommandFailedException(String[] command, List<String> output, String returnCode) {
		super(formatError(command, output, returnCode));
	}

	private static String formatError(String[] command, List<String> output, String retCode){
		String c = "";
		for(String arg : command){
			c = c + arg + " ";
		}
		return(formatError(c, output, retCode));
	}

		private static String formatError(String command, List<String> output, String retCode){
		String ret = "'" + command + "' did not return properly with return code: '" + retCode + "' full output:";
		for(String o : output){
			ret = ret + "\n\t" + o;
		}
		return(ret);
	}
		
	private static String formatError(String command, String output, String retCode){
		String ret = "'" + command + "' did not return properly with return code: '" + retCode + "' full output: " + output;
		return(ret);
	}
}
