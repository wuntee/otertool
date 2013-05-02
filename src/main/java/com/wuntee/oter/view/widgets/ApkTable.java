package com.wuntee.oter.view.widgets;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.packagemanager.PackageBean;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;

public class ApkTable {
	private static Logger logger = Logger.getLogger(ApkTable.class);

	private Table table;
	
	public ApkTable(Composite parent){
		this(parent, SWT.BORDER | SWT.FULL_SELECTION);
	}
	
	public ApkTable(Composite parent, int style){
		this.table = new Table(parent, style);

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 3, 19);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tblclmnApk = new TableColumn(table, SWT.NONE);
		tblclmnApk.setWidth(200);
		tblclmnApk.setText("APK");
		
		final TableColumn tblclmnPackage = new TableColumn(table, SWT.NONE);
		tblclmnPackage.setWidth(300);
		tblclmnPackage.setText("Package");
		
		final String[] keys = {PackageBean.class.getName()};
		GuiWorkshop.addColumnSorter(table, tblclmnApk, 0, keys);
		GuiWorkshop.addColumnSorter(table, tblclmnPackage, 1, keys);		
	}
	
	public void loadPackages(){
		new Thread(new Runnable(){
			public void run() {
				try {
					List<PackageBean> packageBeans = AdbWorkshop.listPackages();
					loadPackagesGui(packageBeans);
				} catch (Exception e) {
					logger.error("Error loading packages:" ,e);
				}				
			}
		}).start();
	}
	
	private void loadPackagesGui(List<PackageBean> packages){
		table.getDisplay().asyncExec(new Runnable(){
			public void run() {
				table.removeAll();
				
				try {
					List<PackageBean> beans = AdbWorkshop.listPackages();
					for(PackageBean bean : beans){
						String apk = bean.getApk().substring(bean.getApk().lastIndexOf('/')+1, bean.getApk().length());
		                TableItem tableItem = new TableItem(table, SWT.NONE);
		                tableItem.setText(new String[]{apk, bean.getClazz()});
		                tableItem.setData(PackageBean.class.getName(), bean);
					}
				} catch (Exception e) {
					logger.error("Error loading packages:" ,e);
					GuiWorkshop.messageError(table.getShell(), "There was an error loading the packages: " + e.getMessage());
				}		
			}
		});
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}
	
}
