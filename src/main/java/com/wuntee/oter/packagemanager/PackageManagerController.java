package com.wuntee.oter.packagemanager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.aapt.androidmanifest.AndroidManifestObject;
import com.wuntee.oter.aapt.androidmanifest.AndroidManifestWorkshop;
import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.fs.FsDiffController;
import com.wuntee.oter.fs.FsNode;
import com.wuntee.oter.fs.FsWorkshop;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;
import com.wuntee.oter.view.widgets.CTabItemWithDatabase;
import com.wuntee.oter.view.widgets.CTabItemWithHexViewer;
import com.wuntee.oter.view.widgets.CTabItemWithStyledText;
import com.wuntee.oter.view.widgets.runnable.FileToStyledTextRunnable;
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
            PackageBean bean = (PackageBean) t.getData(PackageBean.class.getName());
			String packageClazz = bean.getClazz();
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
	
	public void loadFileContentsToTab(FsNode node) throws Exception{
		logger.debug("Node selected: " + node.getFullPath());
		if(!node.isDirectory()){
			gui.setStatus("Loading file " + node.getName());
			File f = AdbWorkshop.pullFile(node.getFullPath());
			// Try to load a database
			if(node.getName().endsWith(".db")){
				CTabItemWithDatabase item = new CTabItemWithDatabase(gui.getPackageManagerFilesTabs(), node.getName(), f, SWT.CLOSE);
			} else if(node.getName().endsWith(".xml") || node.getName().endsWith(".txt")){
				CTabItemWithStyledText a = new CTabItemWithStyledText(gui.getPackageManagerFilesTabs(), node.getName(), SWT.CLOSE);
				gui.runRunnableAsync(new FileToStyledTextRunnable(f, a.getStyledText()));				
			} else {
				CTabItemWithHexViewer hexTab = new CTabItemWithHexViewer(gui.getPackageManagerFilesTabs(), node.getName(), f, SWT.CLOSE);
			}
			gui.clearStatus();
		}
	}
	
	public void loadFileContentsToSQLiteTab(FsNode node) throws Exception{
		gui.setStatus("Loading file " + node.getName());
		File f = AdbWorkshop.pullFile(node.getFullPath());
		CTabItemWithDatabase item = new CTabItemWithDatabase(gui.getPackageManagerFilesTabs(), node.getName(), f, SWT.CLOSE);
		gui.clearStatus();
	}
	
	public void loadFileContentsToTextTab(FsNode node) throws Exception{
		gui.setStatus("Loading file " + node.getName());
		File f = AdbWorkshop.pullFile(node.getFullPath());
		CTabItemWithStyledText a = new CTabItemWithStyledText(gui.getPackageManagerFilesTabs(), node.getName(), SWT.CLOSE);
		gui.runRunnableAsync(new FileToStyledTextRunnable(f, a.getStyledText()));
		gui.clearStatus();		
	}
	
	public void loadFileContentsToHexTab(FsNode node) throws Exception{
		gui.setStatus("Loading file " + node.getName());
		File f = AdbWorkshop.pullFile(node.getFullPath());
		CTabItemWithHexViewer hexTab = new CTabItemWithHexViewer(gui.getPackageManagerFilesTabs(), node.getName(), f, SWT.CLOSE);
		gui.clearStatus();				
	}
	
	public void saveFileAs(FsNode node, String filename) throws Exception {
		gui.setStatus("Saving file " + node.getName());
		File f = AdbWorkshop.pullFileTo(node.getFullPath(), filename);
		gui.clearStatus();
	}

	public void loadPackageDetails(TableItem[] selection) {
		
		if(selection.length > 1) {
			return;
		}

		TableItem item = selection[0];
		final PackageBean bean = (PackageBean)item.getData(PackageBean.class.getName());
		logger.info("Loading package details for: " + bean.getApk());
		gui.setStatus("Loading package details for: " + bean.getApk());
		
		// Load file details
		Thread details = new Thread(new Runnable(){
			public void run() {
					// Pull file
					File apk = null;
					try {
						apk = AdbWorkshop.pullFile(bean.getApk());
					} catch (Exception e) {
						logger.error("Could not pull apk from device: ", e);
						GuiWorkshop.messageErrorThreaded(gui, "Could not pull apk from device: " + e.getMessage());
					}
					if(apk != null){
						// Manifest
						try {
							final AndroidManifestObject root = AndroidManifestWorkshop.getAndroidManifestObjectsForApk(apk);
							gui.runRunnableAsync(new Runnable(){
								public void run() {
									// Load data in gui
									gui.getPackageManagerAndroidManifestTab().loadAndroidManifestObjects(root);
								}
							});
								
						} catch (Exception e) {
							logger.error("Could not parse the AndroidManifest.xml file: ", e);
							GuiWorkshop.messageErrorThreaded(gui, "Could not parse the AndroidManifest.xml file: " + e.getMessage());
						}
						// AAPT
						List<String> details = null;
						try {
							details = PackageManagerWorkshop.getDetailedPackageInfo(apk);
						} catch (Exception e) {
							logger.error("Could not get detailed package information: ", e);
							GuiWorkshop.messageErrorThreaded(gui, "Could not get detailed package information: " + e.getMessage());
						}
						final StringBuffer sb = new StringBuffer();
						if(details != null){
							for(String det : details){
								sb.append(det).append("\n");
							}
						}
						gui.runRunnableAsync(new Runnable(){
							public void run() {
								// Load data in gui
								gui.getPackageManagerStyledText().setText(sb.toString());
							}
						});
					}
					// Files
					List<FsNode> files = null;
					try {
						files = FsWorkshop.getDirectoryRecursive("/data/data/" + bean.getClazz() + "/");
					} catch (Exception e) {
						logger.error("Could not get file listing: ", e);
						GuiWorkshop.messageErrorThreaded(gui, "Could not get file listing: " + e.getMessage());
					}
					if(files != null){
						gui.runRunnableAsync(new FsListToTreeRunnable(files, gui.getPackageManagerFilesTree()));
					}
					
					gui.clearStatus();
			}
		});
		details.start();
				
	}

	
}
