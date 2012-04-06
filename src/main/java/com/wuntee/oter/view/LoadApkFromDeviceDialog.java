package com.wuntee.oter.view;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.packagemanager.PackageBean;

public class LoadApkFromDeviceDialog extends Dialog {
	private static Logger logger = Logger.getLogger(LoadApkFromDeviceDialog.class);

	protected PackageBean result;
	protected Shell shlSelectApk;
	private Table table;

	public static String PACKAGE_BEAN = "bean";
	
	public LoadApkFromDeviceDialog(Shell parent) {
		// Pass the default styles here
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public LoadApkFromDeviceDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}
	
	public void loadPackages(){
		
		try {
			List<PackageBean> beans = AdbWorkshop.listPackages();
			for(PackageBean bean : beans){
				String apk = bean.getApk().substring(bean.getApk().lastIndexOf('/')+1, bean.getApk().length());
                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(new String[]{apk, bean.getClazz()});
                tableItem.setData(PACKAGE_BEAN, bean);
			}
		} catch (Exception e) {
			logger.error("Error loading packages:" ,e);
			GuiWorkshop.messageError(shlSelectApk, "There was an error loading the packages: " + e.getMessage());
		}
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public PackageBean open() {
		createContents();
		shlSelectApk.open();
		shlSelectApk.layout();
		Display display = getParent().getDisplay();
		shlSelectApk.update();
		shlSelectApk.redraw();
		
		loadPackages();
		
		while (!shlSelectApk.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlSelectApk = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shlSelectApk.setSize(592, 486);
		shlSelectApk.setText("Select APK");
		shlSelectApk.setLayout(new GridLayout(1, false));
		
		table = new Table(shlSelectApk, SWT.BORDER | SWT.FULL_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				result = (PackageBean)table.getSelection()[0].getData(PACKAGE_BEAN);
				shlSelectApk.close();
			}
		});
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBounds(0, 0, 3, 19);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tblclmnApk = new TableColumn(table, SWT.NONE);
		tblclmnApk.setWidth(240);
		tblclmnApk.setText("APK");
		
		final TableColumn tblclmnPackage = new TableColumn(table, SWT.NONE);
		tblclmnPackage.setWidth(338);
		tblclmnPackage.setText("Package");
		
		final String[] keys = {PACKAGE_BEAN};
		GuiWorkshop.addColumnSorter(table, tblclmnApk, 0, keys);
		GuiWorkshop.addColumnSorter(table, tblclmnPackage, 1, keys);
		
		Composite composite = new Composite(shlSelectApk, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite.setBounds(0, 0, 64, 64);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		composite.setLayout(gl_composite);
		
		Button btnLoad = new Button(composite, SWT.NONE);
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(table.getSelection().length == 0){
					GuiWorkshop.messageError(shlSelectApk, "Please Select an APK to load.");
				} else {
					result = (PackageBean)table.getSelection()[0].getData(PACKAGE_BEAN);
					shlSelectApk.close();
				}
			}
		});
		btnLoad.setText("Load");
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = null;
				shlSelectApk.close();
			}
		});
		btnCancel.setText("Cancel");

	}
}
