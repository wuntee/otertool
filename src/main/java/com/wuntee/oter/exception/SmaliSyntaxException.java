package com.wuntee.oter.exception;

import java.io.File;

public class SmaliSyntaxException extends Exception {
	private static final long serialVersionUID = 1L;

	public SmaliSyntaxException(File f, int errors){
		super("There are syntax errors in: " + f.getName() + " Errors: " + errors);
	}
}
