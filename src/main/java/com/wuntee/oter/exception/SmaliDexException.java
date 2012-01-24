package com.wuntee.oter.exception;

import java.io.File;

public class SmaliDexException extends Exception {
	private static final long serialVersionUID = 1L;

	public SmaliDexException(File f, int errors){
		super("There was an error when adding the file: " + f.getName() + " to the dex file. Errors: " + errors);
	}
}
