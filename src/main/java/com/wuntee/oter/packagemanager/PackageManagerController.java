package com.wuntee.oter.packagemanager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.fs.FsDiffController;
import com.wuntee.oter.fs.FsNode;
import com.wuntee.oter.fs.FsWorkshop;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;
import com.wuntee.oter.view.widgets.CTabItemWithDatabase;
import com.wuntee.oter.view.widgets.CtabItemWithHexViewer;
import com.wuntee.oter.view.widgets.runnable.FsListToTreeRunnable;

public class PackageManagerController {
	private static Logger logger = Logger.getLogger(FsDiffController.class);
	private Gui gui;
	
	public static final String[] ALL_KEYS = {PackageBean.class.getName()};
	
	public PackageManagerController(Gui gui){
		this.gui = gui;
	}
	
	public void pullPackages(String destinationDirectory){
		String error = "";
		for(TableItem tableItem : gui.getApkTable().getTable().getSelection()){
			PackageBean bean = (PackageBean)tableItem.getData(PackageBean.class.getName());
			gui.setStatusBlocking("Pulling: " + bean.getClazz());
			try {
				File dst = new File(destinationDirectory, bean.getClazz() + ".apk");
				dst = AdbWorkshop.pullFileTo(bean.getApk(), dst.getAbsolutePath());
				logger.debug("Pulled: " + dst.getAbsolutePath());
			} catch (Exception e) {
				error = error + "\n\t" + bean.getClazz();
			}
		}
		if(error.equals("")){
			GuiWorkshop.messageDialog(gui.getShell(), "All files have been sucessfully pulled.");
		} else {
			GuiWorkshop.messageError(gui.getShell(), "Could not pull the following files:" + error);
		}
		gui.clearStatus();
	}
	
	public void uninstallPackages(TableItem[] selected){
		String msg = "The following packages have been uninstalled: ";
		for(TableItem t : selected){
			String packageClazz = t.getText(0);
			try{
				AdbWorkshop.uninstallPackage(packageClazz);
				msg = msg + "\n\t" + packageClazz;
			} catch (Exception e){
				GuiWorkshop.messageError(gui.getShell(), "Could not uninstall: " + packageClazz + ", " + e.getMessage());
				logger.error("Could not uninstall: " + packageClazz, e);
			}
		}
		GuiWorkshop.messageDialog(gui.getShell(), msg);
	}
	
	public void setPackageDetails(TableItem[] selected){
		if(selected.length > 1){
			this.gui.getPackageManagerStyledText().setText("Could not display details; multiple packages are selected.");
		} else {
			Thread load = new Thread(new LoadPackageDetails((PackageBean)selected[0].getData(PackageBean.class.getName())));
			load.start();
		}
	}
	
	public void loadPackageDetailsInGui(String details){
		this.gui.runRunnableAsync(new LoadPackageDetaisInGui(details));
	}
	
	public class LoadPackageDetails implements Runnable{
		private PackageBean itemBean;
		public LoadPackageDetails(PackageBean itemBean){
			this.itemBean = itemBean;
		}
		public void run() {
			gui.setStatus("Loading package details: " + itemBean.getClazz());
			try{
				StringBuffer buf = new StringBuffer();
				for(String s : PackageManagerWorkshop.getDetailedPackageInfo(itemBean)){
					buf.append(s).append("\n");
				}
				loadPackageDetailsInGui(buf.toString());
				
			} catch(Exception e){
				gui.getPackageManagerStyledText().setText("Could not get package details: " + e.getMessage());
				logger.error("Could not get package details: ", e);
			}
			gui.clearStatus();
		}		
	}
	public class LoadPackageDetaisInGui implements Runnable{
		String details;
		public LoadPackageDetaisInGui(String details){
			this.details = details;
		}
		public void run() {
			gui.getPackageManagerStyledText().setText(details);
		}
	}
	
	public void loadFileContentsToTab(FsNode node) throws IOException, InterruptedException, CommandFailedException, ClassNotFoundException, SQLException{
		logger.debug("Node selected: " + node.getFullPath());
		if(!node.isDirectory()){
			gui.setStatus("Loading file " + node.getName());
			File f = AdbWorkshop.pullFile(node.getFullPath());
			// Try to load a database
			if(node.getName().endsWith(".db")){
				CTabItemWithDatabase item = new CTabItemWithDatabase(gui.getPackageManagerFilesTabs(), node.getName(), f, SWT.CLOSE);
				
			} else {
				//CTabItemWithStyledText a = new CTabItemWithStyledText(gui.getPackageManagerFilesTabs(), node.getName(), SWT.CLOSE);
				//gui.runRunnableAsync(new FileToStyledTextRunnable(f, a.getStyledText()));
				CtabItemWithHexViewer hexTab = new CtabItemWithHexViewer(gui.getPackageManagerFilesTabs(), node.getName(), f, SWT.CLOSE);
			}
			gui.clearStatus();
		}
	}
	
	public void loadFilesTab(TableItem[] currentSelection) {
		PackageBean sel = (PackageBean)currentSelection[0].getData(PackageBean.class.getName());
		new Thread(new LoadFilesTabThread(sel)).start();
	}
	
	public void loadFilesTabGui(List<FsNode> files){
		this.gui.runRunnableAsync(new FsListToTreeRunnable(files, gui.getPackageManagerFilesTree()));
	}
	
	public class LoadFilesTabThread implements Runnable{
		private PackageBean packageBean;
		public LoadFilesTabThread(PackageBean packageBean){
			this.packageBean = packageBean;
		}
		
		@Override
		public void run() {
			try{
				List<FsNode> files = FsWorkshop.getDirectoryRecursive("/data/data/" + packageBean.getClazz() + "/");
				loadFilesTabGui(files);
			} catch (Exception e){
				logger.error("Could not get files:", e);
			}
		}
	}
	
	
}
