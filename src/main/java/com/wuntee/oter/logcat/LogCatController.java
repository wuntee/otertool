package com.wuntee.oter.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.wuntee.oter.exception.ParseException;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;

public class LogCatController {
	private static Logger logger = Logger.getLogger(LogCatController.class);

	private LogCatCommand logcat;
	private Gui gui;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:S");
	private List<LogCatBean> logcatCurrent;

	public LogCatController(Gui gui){
		this.gui = gui;
		logcatCurrent = new LinkedList<LogCatBean>();
	}
	
	public void copy(){
		TableItem[] selected = this.gui.getLogcatTable().getSelection();
		String clipboard = "";
		for(TableItem item : selected){
			String date = item.getText(0);
			String level = item.getText(1);
			String c = item.getText(2);
			String pid = item.getText(3);
			String message = item.getText(4);
			String line = String.format("%-12s %-5s: %-15s[%5s]: %s\n", date, level, c, pid, message);
			clipboard = clipboard + line;
		}
		GuiWorkshop.setClipboardContents(clipboard);
	}
	
	public void start() throws Exception{
		if(logcat != null && logcat.isRunning())
			logcat.close();

		logcat = new LogCatCommand();
		
		logger.debug("Clearing current list");
		gui.getLogcatTable().clearAll();
		
		logger.debug("Executing command");
		logcat.execute();
		BufferedReader read = logcat.getStdout();
		BufferedReader error = logcat.getStderr();
		
		logger.debug("Creating error thread");
		Runnable errorRunnable = new ErrorLineReaderThread(error);
		Thread errorThread = new Thread(errorRunnable);
		errorThread.start();
		
		logger.debug("Creating adb thread");
		Runnable readRunnable = new AdbLineReaderThread(read);
		Thread readThread = new Thread(readRunnable);
		readThread.start();
	}
	
	public void stop(){
		logger.debug("Stoping logcat");
		if(logcat.isRunning())
			logcat.close();
	}
	
	public void addLine(LogCatBean l){
		this.gui.getDisplay().asyncExec(new AddLineRunnable(l));
	}
	
	public void stopAutoscroll(){
		gui.getLogcatCheckAutoscroll().setSelection(false);
	}
	
	public void reFilterTable(){
		gui.runRunnableAsync(new ReFilterTableRunnable());
	}

	public class ReFilterTableRunnable implements Runnable {
		public void run(){
			Table table = gui.getLogcatTable();
			boolean debug = gui.getLogcatCheckDebug().getSelection();
			logger.debug(debug);
			boolean info = gui.getLogcatCheckInfo().getSelection();
			boolean error = gui.getLogcatCheckError().getSelection();
			boolean warn = gui.getLogcatCheckWarn().getSelection();
			boolean verbose = gui.getLogcatCheckVerbose().getSelection();
			
			table.removeAll();

			for(LogCatBean bean : logcatCurrent){
				if(matchesFilter(bean)){
					if(bean.getLevel() == LogCatLevelEnum.ERROR && error == true){
						TableItem tableItem = new TableItem(table, SWT.NONE);
						tableItem.setText(new String[] {dateFormat.format(bean.getDate()), bean.getLevel().toString(), bean.getClazz(), String.valueOf(bean.getPid()), bean.getMessage()});
						tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					}
					else if(bean.getLevel() == LogCatLevelEnum.WARN && warn == true){
						TableItem tableItem = new TableItem(table, SWT.NONE);
						tableItem.setText(new String[] {dateFormat.format(bean.getDate()), bean.getLevel().toString(), bean.getClazz(), String.valueOf(bean.getPid()), bean.getMessage()});
						tableItem.setForeground(SWTResourceManager.getColor(255, 165, 0));
					}
					else if(bean.getLevel() == LogCatLevelEnum.INFO && info == true){
						TableItem tableItem = new TableItem(table, SWT.NONE);
						tableItem.setText(new String[] {dateFormat.format(bean.getDate()), bean.getLevel().toString(), bean.getClazz(), String.valueOf(bean.getPid()), bean.getMessage()});
						tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
					}
					else if(bean.getLevel() == LogCatLevelEnum.DEBUG && debug == true){
						TableItem tableItem = new TableItem(table, SWT.NONE);
						tableItem.setText(new String[] {dateFormat.format(bean.getDate()), bean.getLevel().toString(), bean.getClazz(), String.valueOf(bean.getPid()), bean.getMessage()});
						tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					}
					else if(bean.getLevel() == LogCatLevelEnum.VERBOSE && verbose == true){
						TableItem tableItem = new TableItem(table, SWT.NONE);
						tableItem.setText(new String[] {dateFormat.format(bean.getDate()), bean.getLevel().toString(), bean.getClazz(), String.valueOf(bean.getPid()), bean.getMessage()});
						tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
					}
				}
			}
			
			int bottom = gui.getLogcatTable().getItemCount();
			gui.getLogcatTable().setTopIndex(bottom - 1);
		}
	}
	
	public boolean matchesFilter(LogCatBean bean){
		try{
			String filter = gui.getLogcatTextFilter().getText().toLowerCase().trim();
			String filterRegex = ".*" + filter + ".*";
			if(filter == ""){
				return(true);
			} else {
				if(bean.getMessage().toLowerCase().matches(filterRegex)){
					return(true);
				} else if(bean.getClazz().toLowerCase().matches(filterRegex)){
					return(true);
				}
			} 
		} catch (Exception e) {
			//Will happen with an invalid regex - just return true
			return(true);
		}
		return(false);
	}
	
	public class AddLineRunnable implements Runnable {
		private LogCatBean l;
		public AddLineRunnable(LogCatBean l){
			this.l = l;
		}
		public void run() {
			logcatCurrent.add(l);
			if(matchesFilter(l)){
				Table logCatTable = gui.getLogcatTable();
				if(l.getLevel() == LogCatLevelEnum.ERROR && gui.getLogcatCheckError().getSelection() == true){
					TableItem tableItem = new TableItem(logCatTable, SWT.NONE);
					tableItem.setText(new String[] {dateFormat.format(l.getDate()), l.getLevel().toString(), l.getClazz(), String.valueOf(l.getPid()), l.getMessage()});
					tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				}
				else if(l.getLevel() == LogCatLevelEnum.WARN && gui.getLogcatCheckWarn().getSelection() == true){
					TableItem tableItem = new TableItem(logCatTable, SWT.NONE);
					tableItem.setText(new String[] {dateFormat.format(l.getDate()), l.getLevel().toString(), l.getClazz(), String.valueOf(l.getPid()), l.getMessage()});
					tableItem.setForeground(SWTResourceManager.getColor(255, 165, 0));
				}
				else if(l.getLevel() == LogCatLevelEnum.INFO && gui.getLogcatCheckInfo().getSelection() == true){
					TableItem tableItem = new TableItem(logCatTable, SWT.NONE);
					tableItem.setText(new String[] {dateFormat.format(l.getDate()), l.getLevel().toString(), l.getClazz(), String.valueOf(l.getPid()), l.getMessage()});
					tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
				}
				else if(l.getLevel() == LogCatLevelEnum.DEBUG && gui.getLogcatCheckDebug().getSelection() == true){
					TableItem tableItem = new TableItem(logCatTable, SWT.NONE);
					tableItem.setText(new String[] {dateFormat.format(l.getDate()), l.getLevel().toString(), l.getClazz(), String.valueOf(l.getPid()), l.getMessage()});
					tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				}
				else if(l.getLevel() == LogCatLevelEnum.VERBOSE && gui.getLogcatCheckVerbose().getSelection() == true){
					TableItem tableItem = new TableItem(logCatTable, SWT.NONE);
					tableItem.setText(new String[] {dateFormat.format(l.getDate()), l.getLevel().toString(), l.getClazz(), String.valueOf(l.getPid()), l.getMessage()});
					tableItem.setForeground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
				}
				// Scroll to the bottom
				if(gui.getLogcatCheckAutoscroll().getSelection() == true){
					int bottom = gui.getLogcatTable().getItemCount();
					gui.getLogcatTable().setTopIndex(bottom - 1);
				}
			}
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
			logger.debug("AdbLinReaderThread run");
			String line = null;
			try{
				while( (line = read.readLine()) != null){
					// Need another try block in order to continue the loop
					try{
						LogCatBean l = LogCatBean.parse(line);
						addLine(l);
					} catch(ParseException e) {
						logger.error("Could not parse: " + line, e);
					}
				}
			} catch(IOException e1){
				logger.error("Could read line", e1);
			}
		}
	}

}
