package com.wuntee.oter.view;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.Collator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.view.bean.BuildAndSignApkBean;

public class GuiWorkshop {
	private static Logger logger = Logger.getLogger(GuiWorkshop.class);

	public static BuildAndSignApkBean selectCertificate(Shell shell){
		BuildAndSignApkDialog dialog = new BuildAndSignApkDialog(shell);
		return(dialog.open());
	}
	
	public static void addColumnSorter(final Table table, TableColumn column, final int index, final String[] dataKeys){
		
	    Listener sortListener = new Listener() {
	        public void handleEvent(Event e) {
	            TableItem[] items = table.getItems();
	            Collator collator = Collator.getInstance(Locale.getDefault());
	            TableColumn column = (TableColumn)e.widget;
	            for (int i = 1; i < items.length; i++) {
	                String value1 = items[i].getText(index);
	                for (int j = 0; j < i; j++){
	                    String value2 = items[j].getText(index);
	                    
	                    if (table.getSortDirection() == SWT.UP || table.getSortDirection() == SWT.NONE && collator.compare(value1, value2) < 0){
	                        String[] values = new String[table.getColumnCount()];
	                        for(int k=0; k<table.getColumnCount(); k++){
	                        	values[k] = items[i].getText(k);
	                        }
	                        // Get data from list item
	                        Map<String, Object> dataMap = new HashMap<String, Object>();
	                        for(String key : dataKeys){
	                        	dataMap.put(key, items[i].getData(key));
	                        }
	                        items[i].dispose();
	                        TableItem item = new TableItem(table, SWT.NONE, j);
	                        item.setText(values);
	                        // Set data for new item
	                        for(String key : dataMap.keySet()){
	                        	item.setData(key, dataMap.get(key));
	                        }
	                        items = table.getItems();
	                        break;
	                    } else if(table.getSortDirection() == SWT.DOWN && collator.compare(value1, value2) > 0){
	                        String[] values = new String[table.getColumnCount()];
	                        for(int k=0; k<table.getColumnCount(); k++){
	                        	values[k] = items[i].getText(k);
	                        }
	                        // Get data from list item
	                        Map<String, Object> dataMap = new HashMap<String, Object>();
	                        for(String key : dataKeys){
	                        	dataMap.put(key, items[i].getData(key));
	                        }
	                        items[i].dispose();
	                        TableItem item = new TableItem(table, SWT.NONE, j);
	                        item.setText(values);
	                        // Set data for new item
	                        for(String key : dataMap.keySet()){
	                        	item.setData(key, dataMap.get(key));
	                        }
	                        items = table.getItems();
	                        break;	                    	
	                    }
	                }
	            }
	            if(table.getSortDirection() == SWT.NONE || table.getSortDirection() == SWT.UP){
	            	table.setSortDirection(SWT.DOWN);
	            } else {
	            	table.setSortDirection(SWT.UP);
	            }
	            table.setSortColumn(column);
	        }
	    };
	    
	    column.addListener(SWT.Selection, sortListener);
	}
	
	public static String selectDirectory(Shell shell) {
		DirectoryDialog directoryDialog = new DirectoryDialog(shell);

		// directoryDialog.setFilterPath("/");
		directoryDialog.setMessage("Please select a directory");

		String dir = directoryDialog.open();
		return (dir);
	}

	public static String selectFile(Shell shell, String[] filters) {
		FileDialog fileDialog = new FileDialog(shell);
		fileDialog.setText("Please select a file");
		fileDialog.setFilterExtensions(filters);
		return (fileDialog.open());
	}
	
	public static String selectSaveFile(Shell shell, String[] filters){
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setText("Please select a file");
		fileDialog.setFilterExtensions(filters);
		return (fileDialog.open());
	}

	public static void messageDialog(Shell shell, String message) {
		MessageBox messageBox = new MessageBox(shell);
		messageBox.setText("Alert");
		messageBox.setMessage(message);
		messageBox.open();
	}
	
	public static int yesNoDialog(Shell shell, String title, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO);
		messageBox.setText(title);
		messageBox.setMessage(message);
		return(messageBox.open());		
	}

	public static void setClipboardContents(String s) {
		StringSelection stringSelection = new StringSelection(s);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, new ClipboardOwner() {
			public void lostOwnership(Clipboard arg0, Transferable arg1) {
			}
		});
	}

	public static void messageError(Shell shell, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
		messageBox.setText("Error");
		messageBox.setMessage(message);
		messageBox.open();
	}

	public static void messageErrorThreaded(final Gui gui, final String message) {
		gui.getDisplay().asyncExec(new Runnable(){
			public void run(){
				MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage(message);
				messageBox.open();
			}
		});
	}

}
