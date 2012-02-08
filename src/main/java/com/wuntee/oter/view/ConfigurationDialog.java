package com.wuntee.oter.view;

import java.io.FileOutputStream;
import java.util.Properties;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.wuntee.oter.OterStatics;

public class ConfigurationDialog extends Dialog {
	private static Logger logger = Logger.getLogger(ConfigurationDialog.class);

	protected Object result;
	protected Shell shell;
	private Text text;

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

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(450, 143);
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
		
		Label lblAndroidHome = new Label(shell, SWT.NONE);
		lblAndroidHome.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblAndroidHome.setBounds(0, 0, 59, 14);
		lblAndroidHome.setText("Android Home:");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite.setBounds(0, 0, 64, 64);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		
		text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setBounds(0, 0, 64, 19);
		text.setText(OterStatics.getAndroidHome() == null ? "" : OterStatics.getAndroidHome());
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String dir = GuiWorkshop.selectDirectory(shell);
				if(dir != null){
					text.setText(dir);
				}
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setBounds(0, 0, 94, 30);
		btnNewButton.setText("Search");
		
		Button btnDebugLogging = new Button(composite, SWT.CHECK);
		btnDebugLogging.setBounds(0, 0, 93, 18);
		btnDebugLogging.setText("Debug logging");
		new Label(composite, SWT.NONE);
		
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
				
				prop.setProperty(OterStatics.PROPERTY_ANDROID_HOME, text.getText());
				System.setProperty(OterStatics.PROPERTY_ANDROID_HOME, text.getText());
				
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
