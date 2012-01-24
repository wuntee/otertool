package com.wuntee.oter.exception;

public class ParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public ParseException(String raw) {
		super("Could not parse: '" + raw + "'");
	}
}
