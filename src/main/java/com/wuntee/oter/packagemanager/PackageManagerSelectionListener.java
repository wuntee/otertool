package com.wuntee.oter.packagemanager;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.view.Gui;

public class PackageManagerSelectionListener extends SelectionAdapter {
	private static Logger logger = Logger.getLogger(PackageManagerSelectionListener.class);
	private PackageManagerController packageManagerController;
	private TableItem[] currentSelection;
	private String currentTab;
	
	public PackageManagerSelectionListener(PackageManagerController packageManagerController){
		this.packageManagerController = packageManagerController;
		this.currentTab = "Aapt";
	}
	
	public void widgetSelected(SelectionEvent arg0) {
		if(currentSelection != null){
			CTabFolder packageManagerTabFolder = (CTabFolder)arg0.getSource();
			updatePackageManagerTabContents(packageManagerTabFolder.getSelection().getText());
		}
	}
	
	public void updatePackageManagerTabContents(String tabName){
		this.currentTab = tabName;
		
		if(tabName.equals("Aapt")){
			logger.info("Updating aapt tab");
			packageManagerController.setPackageDetails(currentSelection);
		} else if(tabName.equals("Files")){
			packageManagerController.loadFilesTab(currentSelection);
		} else {
			logger.error("The tab selected does not have a configuration ('" + currentTab + ")");
		}
	}

	public TableItem[] getCurrentSelection() {
		return currentSelection;
	}

	public void setCurrentSelection(TableItem[] currentSelection) {
		this.currentSelection = currentSelection;
		updatePackageManagerTabContents(currentTab);
	}
	
}
