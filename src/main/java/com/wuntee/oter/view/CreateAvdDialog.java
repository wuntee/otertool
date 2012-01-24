package com.wuntee.oter.view;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkManager;
import com.wuntee.oter.OterStatics;
import com.wuntee.oter.avd.AvdWorkshop;
import com.wuntee.oter.view.bean.CreateAvdBean;

public class CreateAvdDialog extends Dialog {
	private static Logger logger = Logger.getLogger(CreateAvdDialog.class);

	protected CreateAvdBean result;
	protected Shell shell;
	private Text text;
	private Button btnPersistantStorage;
	private Button btnLaunchAvdAfter;
	private Combo combo;
	private Composite composite;

	public CreateAvdDialog(Shell parent) {
		// Pass the default styles here
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}
	  

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CreateAvdDialog(Shell parent, int style) {
		super(parent, style);
		setText("Create Android Virtual Device");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public CreateAvdBean open() {
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
		shell.setSize(335, 236);
		shell.setText(getText());
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.marginRight = 5;
		gl_shell.marginLeft = 5;
		gl_shell.marginTop = 5;
		gl_shell.verticalSpacing = 0;
		gl_shell.marginWidth = 0;
		gl_shell.horizontalSpacing = 0;
		gl_shell.marginHeight = 0;
		shell.setLayout(gl_shell);
		
		Label lblName = new Label(shell, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name:");
		
		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		new Label(shell, SWT.NONE);
		
		Label lblTarget = new Label(shell, SWT.NONE);
		lblTarget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblTarget.setText("Target:");
		
		combo = new Combo(shell, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		addTargets(combo);
		
		new Label(shell, SWT.NONE);
		
		btnPersistantStorage = new Button(shell, SWT.CHECK);
		btnPersistantStorage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		btnPersistantStorage.setText("Persistant system storage");
		
		btnLaunchAvdAfter = new Button(shell, SWT.CHECK);
		btnLaunchAvdAfter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		btnLaunchAvdAfter.setText("Launch AVD after creation");
		
		new Label(shell, SWT.NONE);

		composite = new Composite(shell, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1));
										
												Button btnCreateAvd = new Button(composite, SWT.NONE);
												btnCreateAvd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
												btnCreateAvd.addSelectionListener(new SelectionAdapter() {
													@Override
													public void widgetSelected(SelectionEvent arg0) {
														boolean exists = false;
														try {
															exists = AvdWorkshop.isAvdExist(text.getText());
														} catch (AndroidLocationException e) {
															logger.error("Problem getting avds: ", e);
														}
														if(text.getText().trim().equals("") || combo.getText().trim().equals("")){
															GuiWorkshop.messageError(shell, "Please set a name, and select a target.");
														} else if(exists) {
															GuiWorkshop.messageError(shell, "There is already and AVD with that name, please use a name that does not already exist.");
														} else {
															result = new CreateAvdBean();
															result.setName(text.getText());
															result.setPersistant(btnPersistantStorage.getSelection());;
															result.setTarget(combo.getText());
															result.setLaunch(btnLaunchAvdAfter.getSelection());
															shell.close();
														}
													}
												});
												btnCreateAvd.setText("Create AVD");
										
										Button btnCancel = new Button(composite, SWT.NONE);
										btnCancel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
										btnCancel.addSelectionListener(new SelectionAdapter() {
											@Override
											public void widgetSelected(SelectionEvent arg0) {
												shell.close();
											}
										});
										btnCancel.setText("Cancel");
		
	}
	
	private void addTargets(Combo combo) {
		ISdkLog sdkLogger = new ISdkLog() {
			public void error(Throwable t, String errorFormat, Object[] args) {
				logger.error("Error: ", t);
			}

			public void warning(String warningFormat, Object[] args) {
				logger.warn(args);
			}

			public void printf(String msgFormat, Object[] args) {
				logger.debug(args);
			}
		};
		SdkManager sdkManager = SdkManager.createManager(OterStatics.getAndroidHome(), sdkLogger);
		IAndroidTarget[] targets = sdkManager.getTargets();
		for(IAndroidTarget target : targets){
			combo.add(target.hashString());
		}
	}
}
