package com.wuntee.oter.view;

import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.adb.AdbWorkshop;

import org.eclipse.swt.graphics.Point;

public class ConfigurationDialog extends Dialog {
	private static Logger logger = Logger.getLogger(ConfigurationDialog.class);

	protected Object result;
	protected Shell shell;
	private Text androidHomeText;
	private List javaToSmaliClasspath;
	private Text maxLogcatLinesText;
	private List devicesList;

	public ConfigurationDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ConfigurationDialog(Shell parent, int style) {
		super(parent, style);
		setText("Configuration");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		loadConfig();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	private void loadConfig(){
		// Load current devices
		for(String d : AdbWorkshop.getDeviceList()){
			devicesList.add(d);
		}
		
		// Load Home
		androidHomeText.setText(OterWorkshop.getProperty(OterStatics.PROPERTY_ANDROID_HOME) == null ? "" : OterWorkshop.getProperty(OterStatics.PROPERTY_ANDROID_HOME));
		
		// Load locat lines
		maxLogcatLinesText.setText(OterWorkshop.getProperty(OterStatics.PROPERTY_LOGCAT_MAXLINES));
		
		// Load classpath
		String javaToSmaliClasspathString = OterWorkshop.getProperty(OterStatics.PROPERTY_JAVATOSMALI_CLASSPATH);
		if(javaToSmaliClasspathString != null){
			for(String s : javaToSmaliClasspathString.split(":")){
				if(!s.trim().equals("")){
					javaToSmaliClasspath.add(s);
				}
			}
		}
		
		// Load current device
		String device = OterWorkshop.getProperty(OterStatics.PROPERTY_DEVICE);
		if(device != null){
			for(int i=0; i < devicesList.getItemCount(); i++){
				if(devicesList.getItem(i).equals(device)){
					devicesList.select(i);
				}
			}
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setMinimumSize(new Point(450, 286));
		shell.setSize(450, 394);
		shell.setText("Configuration");
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.verticalSpacing = 0;
		gl_shell.marginWidth = 0;
		gl_shell.marginTop = 5;
		gl_shell.marginRight = 5;
		gl_shell.marginBottom = 5;
		gl_shell.marginLeft = 5;
		gl_shell.marginHeight = 0;
		gl_shell.horizontalSpacing = 0;
		shell.setLayout(gl_shell);
		
		Composite composite_4 = new Composite(shell, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_4 = new GridLayout(1, false);
		gl_composite_4.verticalSpacing = 0;
		gl_composite_4.marginWidth = 0;
		gl_composite_4.marginHeight = 0;
		gl_composite_4.horizontalSpacing = 0;
		composite_4.setLayout(gl_composite_4);
		
		Label lblAndroidHome = new Label(composite_4, SWT.SHADOW_NONE);
		lblAndroidHome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblAndroidHome.setBounds(0, 0, 124, 14);
		lblAndroidHome.setText("Android SDK Location:");
		
		Composite composite = new Composite(composite_4, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		composite.setBounds(0, 0, 440, 28);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		
		androidHomeText = new Text(composite, SWT.BORDER);
		androidHomeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		androidHomeText.setBounds(0, 0, 64, 19);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String dir = GuiWorkshop.selectDirectory(shell);
				if(dir != null){
					androidHomeText.setText(dir);
				}
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setBounds(0, 0, 94, 30);
		btnNewButton.setText("Search");
		
		Composite composite_5 = new Composite(shell, SWT.NONE);
		GridLayout gl_composite_5 = new GridLayout(2, false);
		gl_composite_5.verticalSpacing = 0;
		gl_composite_5.marginWidth = 0;
		gl_composite_5.horizontalSpacing = 0;
		gl_composite_5.marginHeight = 0;
		composite_5.setLayout(gl_composite_5);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMaxLogcatLines = new Label(composite_5, SWT.NONE);
		lblMaxLogcatLines.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMaxLogcatLines.setBounds(0, 0, 59, 14);
		lblMaxLogcatLines.setText("Max LogCat lines:");
		
		maxLogcatLinesText = new Text(composite_5, SWT.BORDER);
		GridData gd_maxLogcatLinesText = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_maxLogcatLinesText.widthHint = 100;
		maxLogcatLinesText.setLayoutData(gd_maxLogcatLinesText);
		maxLogcatLinesText.setBounds(0, 0, 64, 19);
		
		Composite composite_2 = new Composite(shell, SWT.NONE);
		GridLayout gl_composite_2 = new GridLayout(1, false);
		gl_composite_2.marginTop = 5;
		gl_composite_2.verticalSpacing = 0;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.marginHeight = 0;
		gl_composite_2.horizontalSpacing = 0;
		composite_2.setLayout(gl_composite_2);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblJavaToSmali = new Label(composite_2, SWT.NONE);
		lblJavaToSmali.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblJavaToSmali.setText("Java to Smali Classpath:");
		
		javaToSmaliClasspath = new List(composite_2, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_javaToSmaliClasspath = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_javaToSmaliClasspath.heightHint = 100;
		javaToSmaliClasspath.setLayoutData(gd_javaToSmaliClasspath);
		
		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(2, false);
		gl_composite_3.horizontalSpacing = 0;
		gl_composite_3.marginHeight = 0;
		gl_composite_3.marginWidth = 0;
		gl_composite_3.verticalSpacing = 0;
		composite_3.setLayout(gl_composite_3);
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAdd = new Button(composite_3, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String fileName = GuiWorkshop.selectFile(shell, new String[]{"*.jar"});
				if(fileName != null){
					javaToSmaliClasspath.add(fileName);
				}
			}
		});
		btnAdd.setText("Add");
		
		Button btnRemove = new Button(composite_3, SWT.NONE);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				javaToSmaliClasspath.remove(javaToSmaliClasspath.getSelectionIndex());
			}
		});
		btnRemove.setText("Remove");
		
		Composite composite_6 = new Composite(shell, SWT.NONE);
		GridLayout gl_composite_6 = new GridLayout(1, false);
		gl_composite_6.verticalSpacing = 0;
		gl_composite_6.marginWidth = 0;
		gl_composite_6.marginHeight = 0;
		gl_composite_6.horizontalSpacing = 0;
		composite_6.setLayout(gl_composite_6);
		composite_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label lblDevice = new Label(composite_6, SWT.NONE);
		lblDevice.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblDevice.setBounds(0, 0, 59, 14);
		lblDevice.setText("Device:");
		
		devicesList = new List(composite_6, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_list = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_list.heightHint = 75;
		devicesList.setLayoutData(gd_list);
		devicesList.setBounds(0, 0, 3, 66);
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, true, 1, 1));
		composite_1.setBounds(0, 0, 64, 64);
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.horizontalSpacing = 0;
		gl_composite_1.marginHeight = 0;
		gl_composite_1.marginWidth = 0;
		gl_composite_1.verticalSpacing = 0;
		composite_1.setLayout(gl_composite_1);
		
		Button btnSave = new Button(composite_1, SWT.NONE);
		btnSave.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String configFile = OterStatics.getConfigFileName();
				Properties prop = new Properties();
				
				prop.setProperty(OterStatics.PROPERTY_ANDROID_HOME, androidHomeText.getText());
				
				String cp = StringUtils.join(javaToSmaliClasspath.getItems(), ":");
				prop.setProperty(OterStatics.PROPERTY_JAVATOSMALI_CLASSPATH, cp);
				
				String max = maxLogcatLinesText.getText();
				prop.setProperty(OterStatics.PROPERTY_LOGCAT_MAXLINES, max);
				
				String[] devices = devicesList.getSelection();
				if(devices.length > 0){
					prop.setProperty(OterStatics.PROPERTY_DEVICE, devices[0]);
				}			
				
				try {
					prop.store(new FileOutputStream(configFile), null);
				} catch (Exception e) {
					GuiWorkshop.messageError(shell, "Could not save configuration file(" + configFile + "): " + e.getMessage());
					logger.error("Could not save config: ", e);
				}
				shell.close();
			}
		});
		btnSave.setText("Save");
		
		Button btnCancel = new Button(composite_1, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
		});
		btnCancel.setText("Cancel");

	}
}
