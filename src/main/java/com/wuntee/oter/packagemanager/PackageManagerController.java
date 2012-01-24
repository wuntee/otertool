package com.wuntee.oter.packagemanager;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.fs.FsDiffController;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;

public class PackageManagerController {
	private static Logger logger = Logger.getLogger(FsDiffController.class);
	private Gui gui;
	
	public static final String PACKAGE_BEAN = "packagebean";
	public static final String[] ALL_KEYS = {PACKAGE_BEAN};
	
	public PackageManagerController(Gui gui){
		this.gui = gui;
	}
	
	public void loadPackages(){
		this.gui.getPackageManagerTable().removeAll();
		this.gui.getPackageManagerStyledText().setText("");

		Thread first = new Thread(new LoadPacakges());
		first.start();
	}
	
	public void pullPackages(String destinationDirectory){
		String error = "";
		for(TableItem tableItem : gui.getPackageManagerTable().getSelection()){
			PackageBean bean = (PackageBean)tableItem.getData(PACKAGE_BEAN);
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
			Thread load = new Thread(new LoadPackageDetails((PackageBean)selected[0].getData(PACKAGE_BEAN)));
			load.start();
		}
	}
	
	public void loadPackagesInGui(List<PackageBean> list){
		this.gui.runRunnableAsync(new LoadPackagesInGui(list));
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

	
	public class LoadPacakges implements Runnable{
		public void run() {
			gui.setStatus("Loading package list");
			try{
				loadPackagesInGui(AdbWorkshop.listPackages());
				gui.clearStatus();
			} catch(Exception e){
				//GuiWorkshop.messageError(gui.getShell(), "Could not parse pacakges: " + e.getMessage());
				gui.setStatus("There was an error parsing pacakges.");
				logger.error("Error loading package list: ", e);
			}
			
		}
	}
	public class LoadPackagesInGui implements Runnable{
		private Table table;
		private List<PackageBean> list;
		public LoadPackagesInGui(List<PackageBean> list){
			this.table = gui.getPackageManagerTable();
			this.list = list;
		}
		public void run() {
			for(PackageBean bean : list){
                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(new String[]{bean.getClazz()});
                tableItem.setData(PACKAGE_BEAN, bean);
			}			
		}
		
	}	
}
