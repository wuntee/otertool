package com.wuntee.oter.logcat;

import java.io.BufferedReader;

import org.apache.log4j.Logger;

import com.wuntee.oter.adb.BackgroundAdbCommand;

public class LogCatCommand extends BackgroundAdbCommand{
	private static Logger logger = Logger.getLogger(LogCatBean.class);

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public LogCatCommand(){
		super("logcat");		
	}
	
	public static void main(String args[]) throws Exception{
		LogCatCommand lc = new LogCatCommand();
		logger.debug("Executing command");
		lc.execute();
		BufferedReader read = lc.getStdout();
		BufferedReader error = lc.getStderr();
		
		logger.debug("Creating error thread");
		Runnable errorRunnable = lc.new ErrorLineReaderThread(error);
		Thread errorThread = new Thread(errorRunnable);
		errorThread.start();
		logger.debug("Creating adb thread");
		Runnable readRunnable = lc.new AdbLineReaderThread(read);
		Thread readThread = new Thread(readRunnable);
		readThread.start();
		
		while(true){
		}
				
	}
	
	public class ErrorLineReaderThread implements Runnable {
		private BufferedReader read;
		public ErrorLineReaderThread(BufferedReader read){
			this.read = read;
		}
		
		public void run() {
			String line = null;
			try{
				while( (line = read.readLine()) != null){
					System.err.println("ERROR: " + line);
				}
			} catch(Exception e) {
				System.out.println("Could not parse: " + line);
				e.printStackTrace();
			}
		}
	}
	
	public class AdbLineReaderThread implements Runnable {
		private BufferedReader read;
		public AdbLineReaderThread(BufferedReader read){
			this.read = read;
		}
		
		public void run() {
			String line = null;
			try{
				while( (line = read.readLine()) != null){
					LogCatBean l = LogCatBean.parse(line);
					if(l.getLevel() == LogCatLevelEnum.DEBUG)
						System.out.println(ANSI_BLUE + l + ANSI_RESET);
					else if(l.getLevel() == LogCatLevelEnum.INFO)
						System.out.println(ANSI_BLUE + l + ANSI_RESET);
					else if(l.getLevel() == LogCatLevelEnum.WARN)
						System.out.println(ANSI_YELLOW + l + ANSI_RESET);
					else if(l.getLevel() == LogCatLevelEnum.ERROR)
						System.out.println(ANSI_RED + l + ANSI_RESET);
				}
			} catch(Exception e) {
				System.out.println("Could not parse: " + line);
				e.printStackTrace();
			}
		}
	}
}


