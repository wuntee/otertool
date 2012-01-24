package com.wuntee.oter.exception;

public class NotRootedException  extends Exception {
	private static final long serialVersionUID = 1L;

	public NotRootedException(){
		super("The device is not rooted, and must be for the action to take place.");
	}

}
