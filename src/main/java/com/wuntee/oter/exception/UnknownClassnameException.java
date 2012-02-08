package com.wuntee.oter.exception;

public class UnknownClassnameException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownClassnameException(){
		super("Could not determine the class name for the java source.");
	}
}
