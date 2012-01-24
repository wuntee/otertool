package com.wuntee.oter.view;

import java.io.File;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import sun.security.tools.KeyStoreUtil;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.smali.SmaliWorkshop;
import com.wuntee.oter.view.bean.BuildAndSignApkBean;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

public class BuildAndSignApkDialog extends Dialog {
	private static Logger logger = Logger.getLogger(BuildAndSignApkDialog.class);

	protected BuildAndSignApkBean result;
	protected Shell shlBuildAndSign;
	private Text certFileTextbox;
	private Text passwordTextbox;
	private Table aliasTable;
	private Text apkFileTextbox;
	private Button btnSelectFile;
	private Label lblKeystoreFile;
	private Label lblCertificate;
	private Label lblPassword;
	
	private Control[] keystoreObj;
	private Control[] signObj;
	private Composite composite_1;
	private Composite composite_2;
	private Composite composite_3;
	private Button btnSignApk;

	
	public BuildAndSignApkDialog(Shell parent) {
		// Pass the default styles here
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}
	  
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BuildAndSignApkDialog(Shell parent, int style) {
		super(parent, style);
		setText("Select certificate");
		result = new BuildAndSignApkBean();
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public BuildAndSignApkBean open() {
		createContents();
		shlBuildAndSign.open();
		shlBuildAndSign.layout();
		Display display = getParent().getDisplay();
		while (!shlBuildAndSign.isDisposed()) {
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
		shlBuildAndSign = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shlBuildAndSign.setSize(420, 471);
		shlBuildAndSign.setText("Build and sign APK");
		GridLayout gl_shlBuildAndSign = new GridLayout(1, false);
		gl_shlBuildAndSign.horizontalSpacing = 0;
		gl_shlBuildAndSign.marginBottom = 5;
		gl_shlBuildAndSign.marginRight = 5;
		gl_shlBuildAndSign.marginLeft = 5;
		gl_shlBuildAndSign.verticalSpacing = 0;
		gl_shlBuildAndSign.marginWidth = 0;
		gl_shlBuildAndSign.marginTop = 5;
		gl_shlBuildAndSign.marginHeight = 0;
		shlBuildAndSign.setLayout(gl_shlBuildAndSign);
		
		Label lblDestinationApkFile = new Label(shlBuildAndSign, SWT.NONE);
		lblDestinationApkFile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblDestinationApkFile.setText("Destination APK file:");
		
		Composite composite = new Composite(shlBuildAndSign, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		apkFileTextbox = new Text(composite, SWT.BORDER);
		apkFileTextbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnSelectFile = new Button(composite, SWT.NONE);
		btnSelectFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSelectFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String file = GuiWorkshop.selectSaveFile(getParent(), new String[]{"*.apk"});
				if(file != null){
					apkFileTextbox.setText(file);
				}
			}
		});
		btnSelectFile.setText("Select File");
		
		btnSignApk = new Button(shlBuildAndSign, SWT.CHECK);
		btnSignApk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(btnSignApk.getSelection() == false){
					for(Control c : signObj){
						logger.debug(c);
						c.setEnabled(false);
					}
				}
				else {
					for(Control c : signObj){
						c.setEnabled(true);
					}
				}

			}
		});
		btnSignApk.setText("Sign APK");
		new Label(shlBuildAndSign, SWT.NONE);
		
		final Button btnGenerateKeystoreOn = new Button(shlBuildAndSign, SWT.CHECK);
		btnGenerateKeystoreOn.setEnabled(false);
		btnGenerateKeystoreOn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		btnGenerateKeystoreOn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(btnGenerateKeystoreOn.getSelection() == true){
					for(Control c : keystoreObj){
						logger.debug(c);
						c.setEnabled(false);
					}
				}
				else {
					for(Control c : keystoreObj){
						c.setEnabled(true);
					}
				}
			}
		});
		btnGenerateKeystoreOn.setText("Automatically generate keystore");

		lblKeystoreFile = new Label(shlBuildAndSign, SWT.NONE);
		lblKeystoreFile.setEnabled(false);
		lblKeystoreFile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblKeystoreFile.setText("Keystore file:");
		
		composite_1 = new Composite(shlBuildAndSign, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_1 = new GridLayout(3, false);
		gl_composite_1.verticalSpacing = 0;
		gl_composite_1.marginWidth = 0;
		gl_composite_1.marginHeight = 0;
		gl_composite_1.horizontalSpacing = 0;
		composite_1.setLayout(gl_composite_1);
		
		certFileTextbox = new Text(composite_1, SWT.BORDER);
		certFileTextbox.setEnabled(false);
		certFileTextbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnNewButton = new Button(composite_1, SWT.NONE);
		btnNewButton.setEnabled(false);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String file = GuiWorkshop.selectFile(getParent(), new String[]{"*"});
				if(file != null){
					certFileTextbox.setText(file);
				}
			}
		});
		btnNewButton.setText("Select File");
		new Label(composite_1, SWT.NONE);
		
		lblPassword = new Label(shlBuildAndSign, SWT.NONE);
		lblPassword.setEnabled(false);
		lblPassword.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblPassword.setText("Password:");
		
		composite_2 = new Composite(shlBuildAndSign, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_2 = new GridLayout(1, false);
		gl_composite_2.marginBottom = 5;
		gl_composite_2.marginTop = 5;
		gl_composite_2.marginHeight = 0;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.verticalSpacing = 0;
		composite_2.setLayout(gl_composite_2);
		
		passwordTextbox = new Text(composite_2, SWT.BORDER | SWT.PASSWORD);
		passwordTextbox.setEnabled(false);
		passwordTextbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		passwordTextbox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				// Load the keystore
				try{
					KeyStore ks = KeyStore.getInstance(KeyStoreUtil.niceStoreTypeName(KeyStore.getDefaultType()));
					ks.load(new File(certFileTextbox.getText()).toURI().toURL().openStream(), passwordTextbox.getTextChars());
					Enumeration<String> aliases = ks.aliases();
					aliasTable.clearAll();
					while(aliases.hasMoreElements()){
						String alias = aliases.nextElement();
						
						Certificate cert = ks.getCertificate(alias);
						
		                byte[] encCertInfo = cert.getEncoded();
		                MessageDigest md = MessageDigest.getInstance("MD5");
		                byte[] digest = md.digest(encCertInfo);
		                String key = toHexString(digest);
		                
		                TableItem tableItem = new TableItem(aliasTable,SWT.NONE);
		                
		                tableItem.setText(new String[]{alias, ks.getCreationDate(alias).toString(), key});
					}

				} catch(Exception e) {
					// Do nothing
				}
			}
		});
		
		lblCertificate = new Label(shlBuildAndSign, SWT.NONE);
		lblCertificate.setEnabled(false);
		lblCertificate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblCertificate.setText("Certificate:");
		
		aliasTable = new Table(shlBuildAndSign, SWT.BORDER | SWT.FULL_SELECTION);
		aliasTable.setEnabled(false);
		GridData gd_aliasTable = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_aliasTable.heightHint = 72;
		aliasTable.setLayoutData(gd_aliasTable);
		aliasTable.setHeaderVisible(true);
		aliasTable.setLinesVisible(true);
		
		TableColumn tblclmnName = new TableColumn(aliasTable, SWT.NONE);
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");
		
		TableColumn tblclmnDate = new TableColumn(aliasTable, SWT.NONE);
		tblclmnDate.setWidth(100);
		tblclmnDate.setText("Date");
		
		TableColumn tblclmnChecksum = new TableColumn(aliasTable, SWT.NONE);
		tblclmnChecksum.setWidth(149);
		tblclmnChecksum.setText("Checksum");
		
		composite_3 = new Composite(shlBuildAndSign, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(2, false);
		gl_composite_3.horizontalSpacing = 0;
		gl_composite_3.marginWidth = 0;
		gl_composite_3.marginHeight = 0;
		gl_composite_3.verticalSpacing = 0;
		composite_3.setLayout(gl_composite_3);
		composite_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		
		Button btnSelect = new Button(composite_3, SWT.NONE);
		btnSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(apkFileTextbox.getText().trim().equals("")){
					GuiWorkshop.messageError(shlBuildAndSign, "Please select a filename.");
					return;
				}
				
				// If we want to sign
				if(btnSignApk.getSelection() == true) {
					// If we need to generate a keystore
					if(btnGenerateKeystoreOn.getSelection() == true){
						try{
							String alias = OterStatics.SOME_STRING;
							String password = OterStatics.SOME_STRING;
							KeyStore ks = SmaliWorkshop.createKeystoreWithSecretKey(alias);
							File ksFile = SmaliWorkshop.writeKeystoreToTemporaryFile(ks, password);
							result.setApkFilename(apkFileTextbox.getText());
							result.setSign(btnSignApk.getSelection());
							result.setCertFilename(ksFile.getAbsolutePath());
							result.setPassword(password);
							result.setCertAlias(alias);
							shlBuildAndSign.close();
						} catch(Exception e) {
							logger.error("Error generating keystore: ", e);
							GuiWorkshop.messageError(shlBuildAndSign, "There was an error generating the keystore: " + e.getMessage());
						}
					// If we dont need to generate a keystore
					} else {
						if(aliasTable.getSelection().length > 0){
							result.setApkFilename(apkFileTextbox.getText());
							result.setSign(btnSignApk.getSelection());
							result.setCertFilename(certFileTextbox.getText());
							result.setPassword(passwordTextbox.getText());
							result.setCertAlias(aliasTable.getSelection()[0].getText(0));
							result.setSign(btnSignApk.getSelection());							
							shlBuildAndSign.close();
						} else {
							logger.error("No alias selected.");
							GuiWorkshop.messageError(shlBuildAndSign, "Please select an alias.");
							return;
						}
					}
				// We dont need to sign
				} else {
					result.setApkFilename(apkFileTextbox.getText());
					result.setSign(btnSignApk.getSelection());
					shlBuildAndSign.close();
				}
			}
		});
		btnSelect.setText("Save");
		
		Button btnCancel = new Button(composite_3, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				result = null;
				shlBuildAndSign.close();
			}
		});
		btnCancel.setText("Cancel");
		
		keystoreObj = new Control[]{certFileTextbox, passwordTextbox, aliasTable, btnNewButton, lblKeystoreFile, lblCertificate, lblPassword};
		signObj = new Control[]{btnGenerateKeystoreOn, certFileTextbox, passwordTextbox, aliasTable, btnNewButton, lblKeystoreFile, lblCertificate, lblPassword};
	}
	
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
    
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
}
