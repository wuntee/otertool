package com.wuntee.oter.smali;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.exception.BuildApkException;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.packagemanager.PackageBean;
import com.wuntee.oter.styler.SmaliLineStyler;
import com.wuntee.oter.view.FindDialog;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;
import com.wuntee.oter.view.bean.BuildAndSignApkBean;

public class SmaliController {
	private static Logger logger = Logger.getLogger(SmaliController.class);
	
	private Map<String, File> smaliMap;
	private File currentApk;
	private File baksmaliDir;
	
	public static final String FILENAME = "filename";
	public static final String LINENUMBER = "linenum";
	public static final String CLASS = "class";
	public static final String NAME = "name";
	public static final String PACKAGE = "package";
	public static final String MODIFIED = "modified";
	public static final String STYLED_TEXT = "styledtext";
	public static final String ORIGINAL_TEXT = "originaltext";
	
	private Gui gui;

	public SmaliController(Gui gui){
		this.gui = gui;
	}
	
	public void search(){
		String searchString = gui.getSmaliSearchText().getText();
		gui.setStatus("Searching: " + searchString);
		logger.debug("Searching for: '" + searchString + "'");

		if(currentApk == null){
			GuiWorkshop.messageError(gui.getShell(), "Please load an APK prior to searching.");
		} else if(searchString == null || searchString.equals("")){
			GuiWorkshop.messageError(gui.getShell(), "Please enter a string to search for.");
		} else {
			if(gui.getSmaliSearchRegex().getSelection() == true){
				// Make sure we have a correct regex
				try{
					OterStatics.SOME_STRING.matches(searchString);
				} catch(Exception e){
					GuiWorkshop.messageError(gui.getShell(), "Please enter a correct regex: " + e.getMessage());
					return;
				}
			}
			
			gui.runRunnableAsync(new SearchRunnable(searchString));
		}		
	}
	
	private class SearchRunnable implements Runnable {
		private Table table;
		private String searchString;
		public SearchRunnable(String searchString) {
			this.searchString = searchString;
			this.table = gui.getSmaliSearchTable();
			this.table.removeAll();
		}
		public void run() {
			synchronized (baksmaliDir){
				search(baksmaliDir);
			}
			gui.clearStatus();
		}
		private void search(File file){
			if(file.isDirectory()){
				for(File f : file.listFiles()){
					search(f);
				}
			} else {
				searchFile(file);
			}
		}

		private void searchFile(File f) {
			try{
				int start = (int)baksmaliDir.getAbsolutePath().length() + 1;
				String clazz = f.getAbsolutePath().substring(start, f.getAbsolutePath().length()-".smali".length()).replace(System.getProperty("file.separator"), ".");
				//gui.setStatus("Searching: " + clazz);
				logger.debug("Searching for '" + searchString + "' in: " + f.getAbsolutePath());
				FileInputStream fstream = new FileInputStream(f);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				int line = 0;
				String searchStrLine;
				while ((strLine = br.readLine()) != null) {
					searchStrLine = strLine;
					if(gui.getSmaliSearchIgnoreCase().getSelection() == true){
						searchStrLine = strLine.toLowerCase();
						searchString = searchString.toLowerCase();
					}
					if(gui.getSmaliSearchRegex().getSelection() == true){
						//regex
						if(searchStrLine.matches(searchString)){
							addResult(f, line, strLine);
						}
					} else {
						//non regex
						if(searchStrLine.indexOf(searchString) != -1){
							addResult(f, line, strLine);
						}
					}
					line++;
				}
				
				
			} catch (Exception e){
				GuiWorkshop.messageError(gui.getShell(), "Could not search files: " + e.getMessage());
				logger.error("Could not search files:", e);
			}
		}

		private void addResult(File f, int line, String lineContents){
			TableItem ti = new TableItem(table, SWT.NONE);
			int start = (int)baksmaliDir.getAbsolutePath().length() + 1;
			String clazz = f.getAbsolutePath().substring(start, f.getAbsolutePath().length()-".smali".length()).replace(System.getProperty("file.separator"), ".");
			String name = f.getName().substring(0, f.getName().length()-".smali".length());
			String pkg = "";
			if(clazz.length() != name.length()){
				pkg = clazz.substring(0, clazz.length()-name.length()-1);
			}
			logger.debug("Class: " + clazz);
			logger.debug("Pkg: " + pkg);
			logger.debug("Name: " + name);
			ti.setText(new String[]{clazz, lineContents});
			ti.setData(NAME, name);
			ti.setData(FILENAME, clazz);
			ti.setData(LINENUMBER, line);
			ti.setData(CLASS, clazz);
			ti.setData(PACKAGE, pkg);
		}
	}
	
	public void saveTab(CTabItem tab) throws IOException {
		if( (Boolean)tab.getData(MODIFIED) == true){
			String file = (String)tab.getData(FILENAME);
			logger.debug("Saving file: " + file);
			
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			StyledText styledText = (StyledText)tab.getData(STYLED_TEXT);
			out.write(styledText.getText());
			out.flush();
			out.close();
			
			// Change the tab object to show it has been saved
			tab.setData(ORIGINAL_TEXT, styledText.getText());
			tab.setData(MODIFIED, false);
			tab.setText(tab.getText().substring(2));
		}
	}
	
	public void loadApk(File apk){
		Thread x = new Thread(new LoadApkRunnable(apk));
		x.start();
	}
	
	public void loadApkFromDevice(PackageBean bean){
		try {
			File f = AdbWorkshop.pullFile(bean.getApk());
			loadApk(f);
		} catch (Exception e) {
			logger.error("Could not load package: ", e);
			GuiWorkshop.messageError(gui.getShell(), "Could not load package: " + e.getMessage());
		}
		
	}
	
	public void loadSmaliSource(String pkg, String name){
		logger.debug("pkg: " + pkg);
		logger.debug("name: " + name);
		this.gui.runRunnableAsync(new LoadSmaliSource(pkg, name));
	}
	
	public void loadSmaliSourceWithLineNumber(String pkg, String name, int line){
		logger.debug("pkg: " + pkg);
		logger.debug("name: " + name);
		this.gui.runRunnableAsync(new LoadSmaliSource(pkg, name, line));
	}
	
	public File rebuildApk(String filename){
		gui.setStatus("Building APK: " + filename);
		try{
			SmaliWorkshop.buildApk(this.currentApk, this.baksmaliDir, new File(filename));
			GuiWorkshop.messageDialog(gui.getShell(), "The APK has been built.");
		} catch (Exception e) {
			GuiWorkshop.messageError(gui.getShell(), "Could not build APK: " + e.getMessage());
			logger.error("Could not build APK: ", e);
		}
		gui.clearStatus();
		return(new File(filename));
	}
	
	public void rebuildAndSignApk(BuildAndSignApkBean bean){
		try {
			gui.setStatus("Building APK: " + bean.getApkFilename());
			SmaliWorkshop.buildApk(this.currentApk, this.baksmaliDir, new File(bean.getApkFilename()));
			
			try{
				gui.setStatus("Signing APK: " + bean.getApkFilename());
				SmaliWorkshop.signJar(bean.getCertFilename(), bean.getPassword(), bean.getApkFilename(), bean.getCertAlias());

				// Success!!
				GuiWorkshop.messageDialog(gui.getShell(), "The APK has been built and signed.");
				
			} catch (Exception e) {
				GuiWorkshop.messageError(gui.getShell(), "Could not sign APK: " + e.getMessage());
				logger.error("Could not sign APK: ", e);
			}

		} catch (BuildApkException e) {
			GuiWorkshop.messageError(gui.getShell(), "Could not build APK: " + e.getMessage());
			logger.error("Could not build APK: ", e);
		}
		
		

		gui.clearStatus();
	}	
	
	public boolean unsavedFilesOpen(){
		for(CTabItem tab : gui.getSmaliTabFolder().getItems()){
			if(tab.getShowClose() == true){
				if((Boolean)tab.getData(MODIFIED) == true){
					return(true);
				}
			}
		}
		return(false);
	}
	
	private CTabItem getTabByFullClasspath(String classPath){
		for(CTabItem tab : gui.getSmaliTabFolder().getItems()){
			if(tab.getShowClose() == true){
				if(((String)tab.getData(CLASS)).equals(classPath)){
					return(tab);
				}
			}
		}
		return(null);
	}
	
	private StyledText getStyledTextByFullClasspath(String classPath){
		for(CTabItem tab : gui.getSmaliTabFolder().getItems()){
			if(tab.getShowClose() == true){
				if(((String)tab.getData(CLASS)).equals(classPath)){
					return((StyledText)tab.getData(STYLED_TEXT));
				}
			}
		}
		return(null);
	}
	
	private class LoadSmaliSource implements Runnable {
		private int line = -1;
		private String pkg;
		private String name;
		private String full;
		
		public LoadSmaliSource(String pkg, String name, int line){
			this.line = line;
			this.pkg = pkg;
			this.name = name;
			this.full = (pkg.equals("")) ? name : pkg + "." + name;
		}
		
		public LoadSmaliSource(String pkg, String name){
			this.pkg = pkg;
			this.name = name;
			this.full = (pkg.equals("")) ? name : pkg + "." + name;
		}
		public void run(){
			CTabFolder smaliTabFolder = gui.getSmaliTabFolder();
			CTabItem openFileTab = getTabByFullClasspath(full);
			if(openFileTab != null){
				// File has already been opened, just select it
				smaliTabFolder.setSelection(openFileTab);
				
				if(this.line > -1){
					StyledText styledText = getStyledTextByFullClasspath(full);
					if(styledText != null){
						styledText.setTopIndex(this.line);
					}
				}
			} else {
				// File has not already been opened
				
				try{
					//Load file
					File source = smaliMap.get(full);
					logger.debug("Loading: " + full + " : " + source);
					BufferedReader in = new BufferedReader(new FileReader(source));
					String buf = "";
					String line;
					while( (line = in.readLine()) != null){
						buf = buf + "\n" + line;
					}
					
					// Create tab, and add text
					CTabItem tabItem = new CTabItem(smaliTabFolder, SWT.CLOSE);
					
					tabItem.setText(name);
					
					StyledText styledText = new StyledText(smaliTabFolder, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
					styledText.setEditable(true);
					styledText.addLineStyleListener(new SmaliLineStyler());
					tabItem.setControl(styledText);
					
					smaliTabFolder.setSelection(tabItem);

					styledText.setText(buf);
					
					styledText.addModifyListener(new ModifyListener(){
						public void modifyText(ModifyEvent arg0) {
							for(CTabItem tab : gui.getSmaliTabFolder().getItems()){
								if(tab.getShowClose() == true){
									StyledText styledText = ((StyledText)tab.getData(STYLED_TEXT));
									if( styledText == arg0.getSource() ){
										if((Boolean)tab.getData(MODIFIED) == false){
											tab.setData(MODIFIED, true);
											tab.setText("* " + tab.getText());
										} else {
											if(styledText.getText().equals(tab.getData(ORIGINAL_TEXT))){
												tab.setData(MODIFIED, false);
												tab.setText(tab.getText().substring(2));
											}
										}
									}
								}
							}
						}
					});

					addTextMenu(styledText);
					
					if(this.line > -1){
						styledText.setTopIndex(this.line);
					}

					// Initialize data
					tabItem.setData(ORIGINAL_TEXT, buf);
					tabItem.setData(MODIFIED, false);
					tabItem.setData(STYLED_TEXT, styledText);
					tabItem.setData(CLASS, full);
					tabItem.setData(FILENAME, source.getAbsolutePath());
					
				} catch (Exception e) {
					gui.messageError("Could not open load smali: " + e.getMessage());
					logger.error("Could not open load smali:", e);
				}
			}
		}
	}
	
	private class LoadApkRunnable implements Runnable {
		private Tree tree;
		private File apk;
		public LoadApkRunnable(File apk){
			this.apk = apk;
			tree = gui.getSmaliTree();
		}
		public void run(){
			gui.setStatus("Loading APK: " + apk.getAbsolutePath());
			try {
				baksmaliDir = OterWorkshop.createTemporaryDirectory("aat.baksmali");
				smaliMap = SmaliWorkshop.getSmaliSource(apk, baksmaliDir);
				gui.getDisplay().asyncExec(new Runnable(){
					public void run(){
						tree.removeAll();
						
						currentApk = apk;
						// TODO: Close all currently open tabs
						
						for(String c : smaliMap.keySet()){
							int lastSlash = c.lastIndexOf(".");
							String pkg = "";
							String name;
							if(lastSlash == -1){
								name = c;
								TreeItem item = new TreeItem(tree, SWT.NONE);
								item.setText(name);					
							} else {
								pkg = c.substring(0, lastSlash);
								name = c.substring((lastSlash+1), c.length());
								TreeItem existsInTree = getInTree(pkg);
								if(existsInTree != null){
									TreeItem item = new TreeItem(existsInTree, SWT.NONE);
									item.setText(name);					
								} else {
									TreeItem pkgTreeItem = new TreeItem(tree, SWT.NONE);
									pkgTreeItem.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_PACKAGE));
									pkgTreeItem.setText(pkg);					
									TreeItem item = new TreeItem(pkgTreeItem, SWT.NONE);
									item.setText(name);
								}
							}
						}
					}
				});
			} catch (final Exception e) {
				logger.error("Could not load file: ", e);
				GuiWorkshop.messageErrorThreaded(gui, "Could not load file: " + e.getMessage());
			}
			gui.clearStatus();
		}
		private TreeItem getInTree(String pkg){
			
			for(TreeItem i : tree.getItems()){
				if(i.getText().equals(pkg)){
					return(i);
				}
			}
			return(null);
		}
	}
	
	private void addTextMenu(final StyledText styledText) {
		Shell shell = styledText.getShell();
		
		// Create the menu
		Menu menu = new Menu(shell, SWT.POP_UP);

		// Create Edit->Find
		MenuItem findItem = new MenuItem(menu, SWT.NULL);
		findItem.setText("Find");
		findItem.setAccelerator(SWT.MOD1 + 'F');
		findItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				new FindDialog(gui.getShell(), styledText).open();
			}
		});
		
		// Create Edit->Cut
		MenuItem item = new MenuItem(menu, SWT.NULL);
		item.setText("Cut");
		item.setAccelerator(SWT.MOD1 + 'X');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				styledText.cut();
			}
		});
		
		// Create Edit->Copy
		item = new MenuItem(menu, SWT.NULL);
		item.setText("Copy");
		item.setAccelerator(SWT.MOD1 + 'C');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				styledText.copy();
			}
		});

		// Create Edit->Paste
		item = new MenuItem(menu, SWT.NULL);
		item.setText("Paste");
		item.setAccelerator(SWT.MOD1 + 'V');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				styledText.paste();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		// Create Select All
		item = new MenuItem(menu, SWT.NULL);
		item.setText("Select All");
		item.setAccelerator(SWT.MOD1 + 'A');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				styledText.selectAll();
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);

		// Create Save
		item = new MenuItem(menu, SWT.NULL);
		item.setText("Save");
		//item.setAccelerator(SWT.CTRL + 'S');
		item.setAccelerator(SWT.MOD1 + 's');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					saveTab(getOpenSmaliTabByStyledText(styledText));
				} catch (IOException e) {
					GuiWorkshop.messageError(gui.getShell(), "Could not save the file: " + e.getMessage());
					logger.error("Could not save the file: ", e);
				}
			}
		});
		

		styledText.setMenu(menu);
	}
	
	private CTabItem getOpenSmaliTabByStyledText(StyledText item){
		for(CTabItem tab : gui.getSmaliTabFolder().getItems()){
			if(tab.getShowClose() == true){
				if(((StyledText)tab.getData(STYLED_TEXT)).equals(item)){
					return(tab);
				}
			}
		}
		return(null);
	}
	
}
