package com.wuntee.oter.logcat;

public enum LogCatLevelEnum {
	DEBUG, INFO, WARN, ERROR, VERBOSE;
	
	public String toString(){
		return(super.toString().toLowerCase());
	}
}
