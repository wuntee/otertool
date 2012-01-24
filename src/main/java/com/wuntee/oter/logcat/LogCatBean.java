package com.wuntee.oter.logcat;


import java.util.Date;

import org.apache.log4j.Logger;

import com.wuntee.oter.exception.ParseException;

public class LogCatBean {
	private static Logger logger = Logger.getLogger(LogCatBean.class);
	
	private Date date;
	private String raw;
	private LogCatLevelEnum level;
	private String clazz;
	private int pid;
	private String message;
	
	public LogCatBean(){
	}
	
	public LogCatBean(String raw){
		this.raw = raw;
	}
	
	public static LogCatBean parse(String raw) throws ParseException{
		LogCatBean l = new LogCatBean(raw);
		l.parseLine();
		return(l);
	}
	
	public void parseLine() throws ParseException{
		logger.debug("Parsing: " + raw);

		this.date = new Date();

		// W/RequestProcessingService(  231): RPS schedule next update run for 2011-06-16T08:08:58.000-04:00
		// Level
		switch(raw.charAt(0)){
			case 'V':
				level = LogCatLevelEnum.VERBOSE;
				break;
			case 'D':
				level = LogCatLevelEnum.DEBUG;
				break;
			case 'I':
				level = LogCatLevelEnum.INFO;
				break;
			case 'W':
				level = LogCatLevelEnum.WARN;
				break;
			case 'E':
				level = LogCatLevelEnum.ERROR;
				break;
		}

		try{
			// Class
			this.clazz = raw.substring(2, raw.length()).split("\\(")[0];
	
			// PID
			this.pid = Integer.valueOf(raw.substring( (raw.indexOf("(")+1) , (raw.indexOf(")")) ).trim());
	
			//Message
			this.message = raw.substring(raw.indexOf(":")+2, raw.length());
		} catch(Throwable e) {
			// TODO: Could not parse: '--------- beginning of /dev/log/system'
			// TODO: Catch index exceptions
			throw new ParseException(raw);
		
		}
	}
	
	public String toString() {
		return(String.format("%-5s: %-15s[%5s]: %s", level, clazz, pid, message));
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public LogCatLevelEnum getLevel() {
		return level;
	}

	public void setLevel(LogCatLevelEnum level) {
		this.level = level;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
