package com.wuntee.oter.view;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.avd.AvdController;
import com.wuntee.oter.command.BackgroundCommand;
import com.wuntee.oter.fs.FsDiffController;
import com.wuntee.oter.javatosmali.JavaToSmaliController;
import com.wuntee.oter.logcat.LogCatController;
import com.wuntee.oter.packagemanager.PackageBean;
import com.wuntee.oter.packagemanager.PackageManagerController;
import com.wuntee.oter.smali.SmaliController;
import com.wuntee.oter.styler.SmaliLineStyler;
import com.wuntee.oter.view.bean.BuildAndSignApkBean;
import com.wuntee.oter.view.bean.CreateAvdBean;

public class Gui {

	protected Shell shlOterTool;
	private Display display;
	
	private Label statusLabel;
	
	private Table 				logcatTable;
	private Text 				logcatTextFilter;	
	private LogCatController 	logcatController;
	private Button 				logcatCheckAutoscroll;
	private Button 				logcatCheckDebug;
	private Button 				logcatCheckInfo;
	private Button 				logcatCheckWarn;
	private Button 				logcatCheckError;
	private Button 				logcatCheckVerbose;
	
	private FsDiffController	fsDiffController;
	private Tree 				fsDifferencesTree;
	private Tree				fsDiffFirstTree;
	private Tree				fsDiffSecondTree;
	private SashForm 			fsDiffSashForm;
	
	private SmaliController		smaliController;
	private Tree				smaliTree;
	private CTabFolder 			smaliTabFolder;
	private Text 				smaliSearchText;
	private Table 				smaliSearchTable;
	private Button 				smaliSearchIgnoreCase;
	private Button 				smaliSearchRegex;

	private AvdController 		avdController;
	
	private PackageManagerController	packageManagerController;
	private StyledText 			packageManagerStyledText;
	private Table 				packageManagerTable;
	
	private JavaToSmaliController	javaToSmaliController;
	private StyledText				javaToSmaliSmaliStyledText;
	private StyledText				javaToSmaliJavaStyledText;
	
	private CTabFolder tabFolder;	

	private static Logger logger = Logger.getLogger(Gui.class);
	

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		Display.setAppName("Otertool");
		Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		*/
		try {
			Gui window = new Gui();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		this.display = Display.getDefault();
		
		createContents();

		shlOterTool.open();
		shlOterTool.layout();

		createControllers();
		loadConfig();

		while (!shlOterTool.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void createControllers(){
		logcatController = new LogCatController(this);
		avdController = new AvdController(this);
		fsDiffController = new FsDiffController(this);
		smaliController = new SmaliController(this);
		packageManagerController = new PackageManagerController(this);
		javaToSmaliController = new JavaToSmaliController(this);
	}
	
	public void loadConfig(){
		// load config
		if(OterStatics.getAndroidHome() == null){
			GuiWorkshop.messageError(shlOterTool,"Could not load a configuration file, please specify the configuration.");
			new ConfigurationDialog(shlOterTool).open();
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlOterTool = new Shell();
		shlOterTool.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_APP));
		shlOterTool.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				try{
					logcatController.stop();
				} catch (Exception e){
					// Do nothing
				}
			}
		});
		shlOterTool.setMinimumSize(new Point(550, 250));
		shlOterTool.setSize(1000, 600);
		shlOterTool.setText("Otertool");
		shlOterTool.setLayout(new FormLayout());
		
		
		Menu menu = new Menu(shlOterTool, SWT.BAR);
		shlOterTool.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmConfigure = new MenuItem(menu_1, SWT.NONE);
		mntmConfigure.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Object ret = new ConfigurationDialog(shlOterTool).open();
			}
		});
		mntmConfigure.setText("Configure");
		
		new MenuItem(menu_1, SWT.SEPARATOR);
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				System.exit(0);
			}
		});
		mntmExit.setText("Exit");
		
		MenuItem mntmLogcat = new MenuItem(menu, SWT.CASCADE);
		mntmLogcat.setText("LogCat");
		
		Menu menu_2 = new Menu(mntmLogcat);
		mntmLogcat.setMenu(menu_2);
		
		MenuItem mntmStart = new MenuItem(menu_2, SWT.NONE);
		mntmStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					logcatController.start();
				} catch (Exception e) {
					GuiWorkshop.messageError(shlOterTool, "Could not start: " + e.getMessage());
					logger.error("Could not start logcat:", e);
				}
			}
		});
		mntmStart.setText("Start");
		
		MenuItem mntmStop = new MenuItem(menu_2, SWT.NONE);
		mntmStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.stop();
			}
		});
		mntmStop.setText("Stop");
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		MenuItem mntmClear = new MenuItem(menu_2, SWT.NONE);
		mntmClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatTable.removeAll();
			}
		});
		mntmClear.setText("Clear");
		
		MenuItem mntmFsdiff = new MenuItem(menu, SWT.CASCADE);
		mntmFsdiff.setText("FsDiff");
		
		Menu menu_3 = new Menu(mntmFsdiff);
		mntmFsdiff.setMenu(menu_3);
		
		MenuItem mntmScanFirst = new MenuItem(menu_3, SWT.NONE);
		mntmScanFirst.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try{
					fsDiffController.scanFirst();
				} catch (Exception e) {
					GuiWorkshop.messageError(shlOterTool, "Could not scan: " + e.getMessage());
					logger.error("Could not scan:", e);
				}
			}
		});
		mntmScanFirst.setText("Scan First");
		
		MenuItem mntmScanSecond = new MenuItem(menu_3, SWT.NONE);
		mntmScanSecond.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try{
					fsDiffController.scanSecond();
				} catch (Exception e) {
					GuiWorkshop.messageError(shlOterTool, "Could not scan: " + e.getMessage());
					logger.error("Could not scan:", e);
				}
			}
		});
		mntmScanSecond.setText("Scan Second");
		
		MenuItem mntmGenerateDifferences = new MenuItem(menu_3, SWT.NONE);
		mntmGenerateDifferences.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fsDiffController.generateDifferences();
			}
		});
		mntmGenerateDifferences.setText("Generate Differences");
		
		new MenuItem(menu_3, SWT.SEPARATOR);
		
		MenuItem mntmClear_1 = new MenuItem(menu_3, SWT.NONE);
		mntmClear_1.setText("Clear");
		
		MenuItem mntmApktool = new MenuItem(menu, SWT.CASCADE);
		mntmApktool.setText("Smali");
		
		Menu menu_4 = new Menu(mntmApktool);
		mntmApktool.setMenu(menu_4);
		
		MenuItem mntmLoadFile = new MenuItem(menu_4, SWT.NONE);
		mntmLoadFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String file = GuiWorkshop.selectFile(shlOterTool, new String[]{"*.apk"});
				if(file != null){
					smaliController.loadApk(new File(file));
				}
			}
		});
		mntmLoadFile.setText("Load APK");
		
		MenuItem mntmLoadApkDevice = new MenuItem(menu_4, SWT.NONE);
		mntmLoadApkDevice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				PackageBean apkBean = new LoadApkFromDeviceDialog(shlOterTool).open();
				if(apkBean != null){
					smaliController.loadApkFromDevice(apkBean);
				}
			}
		});
		mntmLoadApkDevice.setText("Load APK From Device");
		
		MenuItem mntmBuild = new MenuItem(menu_4, SWT.NONE);
		mntmBuild.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(smaliController.unsavedFilesOpen() == true){
					int save = GuiWorkshop.yesNoDialog(getShell(), "Really build?", "You have unsaved smali files; if you do not save them, they will not be applied to the new APK. Are you sure you want to rebuild the APK without saving?");
					if(save == SWT.NO){
						return;
					}
				}

				BuildAndSignApkBean bean = new BuildAndSignApkDialog(getShell()).open();
				if(bean != null && bean.getApkFilename() != null){
					if(bean.isSign()){
						if(bean.getApkFilename() != null && bean.getCertAlias() != null && bean.getCertFilename() != null && bean.getPassword() != null){
							setStatus("Building and signing APK to: " + bean.getApkFilename());
							smaliController.rebuildAndSignApk(bean);
						}
					} else {
						smaliController.rebuildApk(bean.getApkFilename());
					}
				}
			}
		});
		mntmBuild.setText("Build APK...");
		
		MenuItem mntmJavaToSmali = new MenuItem(menu, SWT.CASCADE);
		mntmJavaToSmali.setText("Java to Smali");
		
		Menu menu_8 = new Menu(mntmJavaToSmali);
		mntmJavaToSmali.setMenu(menu_8);
		
		MenuItem mntmCompile = new MenuItem(menu_8, SWT.NONE);
		mntmCompile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				javaToSmaliController.tryToCompileJava(javaToSmaliJavaStyledText, javaToSmaliSmaliStyledText);
			}
		});
		mntmCompile.setText("Convert Java to Smali");
		
		new MenuItem(menu_8, SWT.SEPARATOR);
		
		MenuItem mntmConfigureClasspath = new MenuItem(menu_8, SWT.NONE);
		mntmConfigureClasspath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Object ret = new ConfigurationDialog(shlOterTool).open();
			}
		});
		mntmConfigureClasspath.setText("Configure classpath");
		
		MenuItem mntmAddAndroidjarTo = new MenuItem(menu_8, SWT.NONE);
		mntmAddAndroidjarTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try{
					OterWorkshop.addAndroidjarToClasspath();
				} catch(Exception e){
					GuiWorkshop.messageError(shlOterTool, e.getMessage());
					return;
				}
				GuiWorkshop.messageDialog(shlOterTool, "Sucessfull added android.jar to classpath. View File->Configure to view changes.");
			}
		});
		mntmAddAndroidjarTo.setText("Add android.jar to classpath");
		
		MenuItem mntmTools = new MenuItem(menu, SWT.CASCADE);
		mntmTools.setText("Tools");
		
		Menu menu_6 = new Menu(mntmTools);
		mntmTools.setMenu(menu_6);
		
		MenuItem mntmLaunchAndroid = new MenuItem(menu_6, SWT.NONE);
		mntmLaunchAndroid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setStatus("Launging android.");
				BackgroundCommand c = new BackgroundCommand(OterStatics.getAndroidCommand());
				try {
					c.execute();
				} catch (Exception e) {
					GuiWorkshop.messageError(shlOterTool, "Could not execute android: " + e.getMessage());
				}
				clearStatus();
			}
		});
		mntmLaunchAndroid.setText("Launch android");
		
		MenuItem mntmRestartAdb = new MenuItem(menu_6, SWT.NONE);
		mntmRestartAdb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setStatus("Restarting ADB");
				try{
					AdbWorkshop.restartAdb();
					GuiWorkshop.messageDialog(shlOterTool, "Adb has been restarted.");
				} catch (Exception e) {
					GuiWorkshop.messageError(shlOterTool, "Could not restart ADB: " + e.getMessage());
					logger.error("Could not restart ADB:", e);
				}
				clearStatus();
			}
		});
		mntmRestartAdb.setText("Restart ADB");
		
		MenuItem mntmInstallCertificate = new MenuItem(menu_6, SWT.NONE);
		mntmInstallCertificate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				String certfile = GuiWorkshop.selectFile(shlOterTool, new String[]{"*"});
				
				if(certfile != null){
					setStatus("Installing certificate: " + certfile);
					try{
						AdbWorkshop.installCert(new File(certfile), "changeit");
						GuiWorkshop.messageDialog(shlOterTool, "The certificate has been sucessfully installed");
					} catch (Exception e) {
						GuiWorkshop.messageError(shlOterTool, "Could not install cert: " + e.getMessage());
						logger.error("Could not install cert:", e);
					}
					clearStatus();
				}
			}
		});
		mntmInstallCertificate.setText("Install Certificate");
		
		MenuItem mntmInstallApk = new MenuItem(menu_6, SWT.NONE);
		mntmInstallApk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				installApk();
			}
		});
		mntmInstallApk.setText("Install APK");
		
		MenuItem mntmCreateAvd = new MenuItem(menu_6, SWT.NONE);
		mntmCreateAvd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//installApk();
				CreateAvdBean ret = new CreateAvdDialog(getShell()).open();
				if(ret != null){
					avdController.createAvd(ret);
				}
			}
		});
		mntmCreateAvd.setText("Create Android Virtual Device");
		
		tabFolder = new CTabFolder(shlOterTool, SWT.BORDER | SWT.BOTTOM );
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String selectedTab = tabFolder.getSelection().getText();
				if(selectedTab.equals("Package Manager") && packageManagerTable.getItemCount() == 0){
					packageManagerController.loadPackages();
				}
			}
		});

		FormData fd_tabFolder = new FormData();
		fd_tabFolder.top = new FormAttachment(0, 3);
		fd_tabFolder.right = new FormAttachment(100);
		fd_tabFolder.left = new FormAttachment(0, 3);
		tabFolder.setLayoutData(fd_tabFolder);
		tabFolder.setSimple(false);
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmLogcat = new CTabItem(tabFolder, SWT.NONE);
		tbtmLogcat.setText("LogCat");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmLogcat.setControl(composite);
		composite.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(3, false);
		gl_composite_1.horizontalSpacing = 0;
		gl_composite_1.marginHeight = 0;
		gl_composite_1.marginWidth = 0;
		gl_composite_1.verticalSpacing = 0;
		composite_1.setLayout(gl_composite_1);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Composite composite_11 = new Composite(composite_1, SWT.NONE);
		composite_11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite_11 = new GridLayout(1, false);
		gl_composite_11.horizontalSpacing = 0;
		gl_composite_11.marginHeight = 0;
		gl_composite_11.marginWidth = 0;
		gl_composite_11.verticalSpacing = 0;
		composite_11.setLayout(gl_composite_11);
		
		Label lblFilter = new Label(composite_11, SWT.HORIZONTAL);
		lblFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblFilter.setText("Message Filter:");
		
		logcatTextFilter = new Text(composite_11, SWT.BORDER);
		logcatTextFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				logcatController.reFilterTable();
			}
		});
		logcatTextFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		GridLayout gl_composite_2 = new GridLayout(8, false);
		gl_composite_2.verticalSpacing = 0;
		gl_composite_2.horizontalSpacing = 0;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.marginHeight = 0;
		composite_2.setLayout(gl_composite_2);
		
		logcatCheckDebug = new Button(composite_2, SWT.CHECK);
		logcatCheckDebug.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.reFilterTable();
			}
		});
		logcatCheckDebug.setSize(57, 18);
		logcatCheckDebug.setSelection(true);
		logcatCheckDebug.setText("Debug");
		
		logcatCheckInfo = new Button(composite_2, SWT.CHECK);
		logcatCheckInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.reFilterTable();
			}
		});
		logcatCheckInfo.setSize(43, 18);
		logcatCheckInfo.setSelection(true);
		logcatCheckInfo.setText("Info");
		
		logcatCheckWarn = new Button(composite_2, SWT.CHECK);
		logcatCheckWarn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.reFilterTable();
			}
		});
		logcatCheckWarn.setSize(49, 18);
		logcatCheckWarn.setSelection(true);
		logcatCheckWarn.setText("Warn");
		
		logcatCheckError = new Button(composite_2, SWT.CHECK);
		logcatCheckError.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.reFilterTable();
			}
		});
		logcatCheckError.setSize(49, 18);
		logcatCheckError.setSelection(true);
		logcatCheckError.setText("Error");
		
		logcatCheckVerbose = new Button(composite_2, SWT.CHECK);
		logcatCheckVerbose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.reFilterTable();
			}
		});
		logcatCheckVerbose.setSize(66, 18);
		logcatCheckVerbose.setSelection(true);
		logcatCheckVerbose.setText("Verbose");
		new Label(composite_2, SWT.NONE);
		new Label(composite_2, SWT.NONE);

		this.logcatCheckAutoscroll = new Button(composite_2, SWT.CHECK);
		logcatCheckAutoscroll.setSize(83, 18);
		logcatCheckAutoscroll.setSelection(true);
		logcatCheckAutoscroll.setText("Auto-scroll");
		
		Composite composite_12 = new Composite(composite_1, SWT.NONE);
		composite_12.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		GridLayout gl_composite_12 = new GridLayout(4, false);
		gl_composite_12.marginLeft = 5;
		gl_composite_12.verticalSpacing = 0;
		gl_composite_12.marginWidth = 0;
		gl_composite_12.horizontalSpacing = 0;
		gl_composite_12.marginHeight = 0;
		composite_12.setLayout(gl_composite_12);
		
		Button btnClear = new Button(composite_12, SWT.NONE);
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatTable.removeAll();
			}
		});
		btnClear.setText("Clear");
		
		Button btnStart = new Button(composite_12, SWT.NONE);
		btnStart.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				try {
					logcatController.start();
				} catch (Exception e) {
					GuiWorkshop.messageError(shlOterTool, "Could not start: " + e.getMessage());
					logger.error("Could not start logcat:", e);
				}				
			}
			
		});
		btnStart.setText("Start");
		
		Button btnStop = new Button(composite_12, SWT.NONE);
		btnStop.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.stop();
			}
		});
		btnStop.setText("Stop");
		new Label(composite_12, SWT.NONE);

		
		logcatTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		logcatTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		logcatTable.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent arg0) {
				//if(arg0.y == logcatTable.getLocation().y)
				//logger.debug(arg0.y + ":" + logcatTable.getSize().y);
				//logcatTable.getBounds().y
				//logcatController.stopAutoscroll();
			}
		});
		logcatTable.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub	
			}
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.stopAutoscroll();
			}
		});
		logcatTable.setHeaderVisible(true);
		logcatTable.setLinesVisible(true);
		
		TableColumn tblclmnDate = new TableColumn(logcatTable, SWT.NONE);
		tblclmnDate.setWidth(111);
		tblclmnDate.setText("Timestamp");
		
		TableColumn tblclmnNewColumn = new TableColumn(logcatTable, SWT.NONE);
		tblclmnNewColumn.setWidth(55);
		tblclmnNewColumn.setText("Level");
		
		TableColumn tblclmnClass = new TableColumn(logcatTable, SWT.NONE);
		tblclmnClass.setWidth(86);
		tblclmnClass.setText("Class");
		
		TableColumn tblclmnPid = new TableColumn(logcatTable, SWT.NONE);
		tblclmnPid.setWidth(34);
		tblclmnPid.setText("PID");
		
		TableColumn tblclmnMessage = new TableColumn(logcatTable, SWT.NONE);
		tblclmnMessage.setWidth(600);
		tblclmnMessage.setText("Message");
		
		Menu menu_7 = new Menu(logcatTable);
		logcatTable.setMenu(menu_7);
		
		MenuItem mntmCopy = new MenuItem(menu_7, SWT.NONE);
		mntmCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatController.copy();
			}
		});
		mntmCopy.setText("Copy");
		
		MenuItem mntmCle = new MenuItem(menu_7, SWT.NONE);
		mntmCle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				logcatTable.removeAll();
			}
		});
		mntmCle.setText("Clear");
		
		statusLabel = new Label(shlOterTool, SWT.NONE);
		fd_tabFolder.bottom = new FormAttachment(statusLabel, -6);
		
		CTabItem tbtmFsdiff = new CTabItem(tabFolder, SWT.NONE);
		tbtmFsdiff.setText("FsDiff");
		
		Composite composite_5 = new Composite(tabFolder, SWT.NONE);
		tbtmFsdiff.setControl(composite_5);
		composite_5.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		fsDiffSashForm = new SashForm(composite_5, SWT.NONE);
		
		Composite composite_6 = new Composite(fsDiffSashForm, SWT.NONE);
		composite_6.setLayout(new FormLayout());
		
		Label lblFirst = new Label(composite_6, SWT.NONE);
		FormData fd_lblFirst = new FormData();
		fd_lblFirst.top = new FormAttachment(1);
		fd_lblFirst.left = new FormAttachment(0);
		lblFirst.setLayoutData(fd_lblFirst);
		lblFirst.setText("First");
		
		fsDiffFirstTree = new Tree(composite_6, SWT.BORDER);
		FormData fd_tree_2 = new FormData();
		fd_tree_2.bottom = new FormAttachment(100);
		fd_tree_2.right = new FormAttachment(100);
		fd_tree_2.top = new FormAttachment(lblFirst, 0);
		fd_tree_2.left = new FormAttachment(0);
		fsDiffFirstTree.setLayoutData(fd_tree_2);
						
		Composite composite_7 = new Composite(fsDiffSashForm, SWT.NONE);
		composite_7.setLayout(new FormLayout());
		
		Label lblSecond = new Label(composite_7, SWT.NONE);
		FormData fd_lblSecond = new FormData();
		fd_lblSecond.top = new FormAttachment(1);
		fd_lblSecond.left = new FormAttachment(0);
		lblSecond.setLayoutData(fd_lblSecond);
		lblSecond.setText("Second");
		
		fsDiffSecondTree = new Tree(composite_7, SWT.BORDER);
		FormData fd_tree_3 = new FormData();
		fd_tree_3.bottom = new FormAttachment(100);
		fd_tree_3.right = new FormAttachment(100);
		fd_tree_3.top = new FormAttachment(lblSecond, 0);
		fd_tree_3.left = new FormAttachment(lblSecond, 0, SWT.LEFT);
		fsDiffSecondTree.setLayoutData(fd_tree_3);
		
		Composite composite_8 = new Composite(fsDiffSashForm, SWT.NONE);
		composite_8.setLayout(new FormLayout());
		
		Label lblDifferences = new Label(composite_8, SWT.NONE);
		FormData fd_lblDifferences = new FormData();
		fd_lblDifferences.top = new FormAttachment(1);
		fd_lblDifferences.left = new FormAttachment(0);
		lblDifferences.setLayoutData(fd_lblDifferences);
		lblDifferences.setText("Differences");
		
		fsDifferencesTree = new Tree(composite_8, SWT.BORDER);
		fsDifferencesTree.setHeaderVisible(true);
		FormData fd_tree_4 = new FormData();
		fd_tree_4.bottom = new FormAttachment(100);
		fd_tree_4.right = new FormAttachment(100);
		fd_tree_4.top = new FormAttachment(lblDifferences, 0);
		fd_tree_4.left = new FormAttachment(lblDifferences, 0, SWT.LEFT);
		fsDifferencesTree.setLayoutData(fd_tree_4);
		
		TreeColumn trclmnName = new TreeColumn(fsDifferencesTree, SWT.NONE);
		trclmnName.setWidth(330);
		trclmnName.setText("Name");
		
		TreeColumn trclmnPermissions = new TreeColumn(fsDifferencesTree, SWT.NONE);
		trclmnPermissions.setWidth(72);
		trclmnPermissions.setText("Permissions");
		
		TreeColumn trclmnGroup = new TreeColumn(fsDifferencesTree, SWT.NONE);
		trclmnGroup.setWidth(50);
		trclmnGroup.setText("Group");
		
		TreeColumn trclmnUser = new TreeColumn(fsDifferencesTree, SWT.NONE);
		trclmnUser.setWidth(50);
		trclmnUser.setText("User");
		
		TreeColumn trclmnSize = new TreeColumn(fsDifferencesTree, SWT.NONE);
		trclmnSize.setWidth(40);
		trclmnSize.setText("Size");
		
		TreeColumn trclmnModified = new TreeColumn(fsDifferencesTree, SWT.NONE);
		trclmnModified.setWidth(140);
		trclmnModified.setText("Modified");
		fsDiffSashForm.setWeights(new int[] {1, 1, 3});
		
		CTabItem tbtmApktool = new CTabItem(tabFolder, SWT.NONE);
		tbtmApktool.setText("Smali");
		
		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmApktool.setControl(composite_3);
		composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_1 = new SashForm(composite_3, SWT.NONE);
		
		smaliTree = new Tree(sashForm_1, SWT.BORDER);
		smaliTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				TreeItem items[] = smaliTree.getSelection();
				if(items.length > 0){
					TreeItem sel = items[0];
					if(sel.getItemCount() == 0){
						String name = sel.getText();
						setStatus("Loading: " + name);
						TreeItem parent = sel.getParentItem();
						String pkg = (parent == null) ? "" : parent.getText();
						smaliController.loadSmaliSource(pkg, name);
					} else {
						sel.setExpanded(true);
					}
					clearStatus();
				}
			}
		});
		
		smaliTabFolder = new CTabFolder(sashForm_1, SWT.BORDER);
		smaliTabFolder.setSimple(false);
		smaliTabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tbtmSearch = new CTabItem(smaliTabFolder, SWT.NONE);
		tbtmSearch.setText("Search");
		
		Composite composite_9 = new Composite(smaliTabFolder, SWT.NONE);
		tbtmSearch.setControl(composite_9);
		GridLayout gl_composite_9 = new GridLayout(1, false);
		gl_composite_9.marginTop = 5;
		gl_composite_9.verticalSpacing = 0;
		gl_composite_9.marginHeight = 0;
		gl_composite_9.horizontalSpacing = 0;
		composite_9.setLayout(gl_composite_9);
		
		smaliSearchText = new Text(composite_9, SWT.BORDER);
		smaliSearchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.character == 13){
					smaliController.search();
				}
			}
		});
		smaliSearchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_10 = new Composite(composite_9, SWT.NONE);
		GridLayout gl_composite_10 = new GridLayout(2, false);
		gl_composite_10.marginBottom = 5;
		gl_composite_10.marginHeight = 0;
		gl_composite_10.verticalSpacing = 0;
		gl_composite_10.marginWidth = 0;
		gl_composite_10.horizontalSpacing = 0;
		composite_10.setLayout(gl_composite_10);
		
		smaliSearchIgnoreCase = new Button(composite_10, SWT.CHECK);
		smaliSearchIgnoreCase.setBounds(0, 0, 93, 18);
		smaliSearchIgnoreCase.setText("Ignore Case");
		
		smaliSearchRegex = new Button(composite_10, SWT.CHECK);
		smaliSearchRegex.setBounds(0, 0, 93, 18);
		smaliSearchRegex.setText("Regex");
		
		smaliSearchTable = new Table(composite_9, SWT.BORDER | SWT.FULL_SELECTION);
		smaliSearchTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				TableItem[] selected = smaliSearchTable.getSelection();
				if(selected.length > 0){
					smaliController.loadSmaliSourceWithLineNumber((String)selected[0].getData(SmaliController.PACKAGE), (String)selected[0].getData(SmaliController.NAME), ((Integer)selected[0].getData(SmaliController.LINENUMBER)).intValue());
				}
			}
		});
		smaliSearchTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		smaliSearchTable.setBounds(0, 0, 3, 19);
		smaliSearchTable.setHeaderVisible(true);
		smaliSearchTable.setLinesVisible(true);
		
		TableColumn tblclmnClass_1 = new TableColumn(smaliSearchTable, SWT.NONE);
		tblclmnClass_1.setWidth(200);
		tblclmnClass_1.setText("Class");
		
		TableColumn tblclmnContents = new TableColumn(smaliSearchTable, SWT.NONE);
		tblclmnContents.setWidth(750);
		tblclmnContents.setText("Contents");
		
		StyledText styledText = new StyledText(smaliTabFolder, SWT.BORDER);
		sashForm_1.setWeights(new int[] {1, 5});
		
		CTabItem tbtmJavaToSmali = new CTabItem(tabFolder, SWT.NONE);
		tbtmJavaToSmali.setText("Java to Smali");
		
		Composite composite_13 = new Composite(tabFolder, SWT.NONE);
		tbtmJavaToSmali.setControl(composite_13);
		composite_13.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(composite_13, SWT.NONE);
		
		Composite composite_14 = new Composite(sashForm, SWT.NONE);
		GridLayout gl_composite_14 = new GridLayout(1, false);
		gl_composite_14.verticalSpacing = 0;
		gl_composite_14.marginWidth = 0;
		gl_composite_14.marginHeight = 0;
		gl_composite_14.horizontalSpacing = 0;
		composite_14.setLayout(gl_composite_14);
		
		Label lblJava = new Label(composite_14, SWT.NONE);
		lblJava.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblJava.setText("Java");
		
		javaToSmaliJavaStyledText = new StyledText(composite_14, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		javaToSmaliJavaStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		javaToSmaliJavaStyledText.setText("import android.util.Log;\n\npublic class OterTool {\n\n	// Youll need to include everything that would exist in a full \n	// java source file. I typically just write a class in Eclipse\n	// and allow it to handle all imports, and paste it here.\n\n	// You will also need to include the android.jar file in the \n	// classpath to use thing like 'Log'. This can be configured\n	// through the configuration dialog (File->Configure) or you\n	// can attempt to have otertool attempt to automatically add it\n	// for you through Java to Smali->Add android.jar to classpath\n\n	public static void main(String[] args) {\n		// Placing a method here, with its arguments will show you \n		// the calling convention, and allow you to easily paste\n		// the code in the smali class\n		oterToolMethod(\"calling argument\");\n		Log.e(\"Tag\", \"Test\");\n	}\n	\n	public static void oterToolMethod(String arg){\n		// You can paste this portion of the smali code directly in\n		// the end of the original package, and call it from everywhere\n		System.out.println(arg);\n	}\n}");
		//javaToSmaliJavaStyledText.addLineStyleListener(new JavaLineStyler());
		
		Composite composite_15 = new Composite(sashForm, SWT.NONE);
		GridLayout gl_composite_15 = new GridLayout(1, false);
		gl_composite_15.marginHeight = 0;
		gl_composite_15.verticalSpacing = 0;
		gl_composite_15.marginWidth = 0;
		gl_composite_15.horizontalSpacing = 0;
		composite_15.setLayout(gl_composite_15);
		
		Label lblSmali = new Label(composite_15, SWT.NONE);
		lblSmali.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblSmali.setText("Smali");
		
		javaToSmaliSmaliStyledText = new StyledText(composite_15, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		javaToSmaliSmaliStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		javaToSmaliSmaliStyledText.addLineStyleListener(new SmaliLineStyler());
		sashForm.setWeights(new int[] {1, 1});
		
		CTabItem tbtmPackageManager = new CTabItem(tabFolder, SWT.NONE);
		tbtmPackageManager.setText("Package Manager");
		
		Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		tbtmPackageManager.setControl(composite_4);
		composite_4.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm packageManagerSashForm = new SashForm(composite_4, SWT.NONE);
		
		packageManagerTable = new Table(packageManagerSashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		packageManagerTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				packageManagerController.setPackageDetails(packageManagerTable.getSelection());
			}
		});
		packageManagerTable.setHeaderVisible(true);
		packageManagerTable.setLinesVisible(true);
		
		TableColumn tblclmnPackageName = new TableColumn(packageManagerTable, SWT.NONE);
		tblclmnPackageName.setWidth(243);
		tblclmnPackageName.setText("Package Name");
		GuiWorkshop.addColumnSorter(packageManagerTable, tblclmnPackageName, 0, PackageManagerController.ALL_KEYS);
		
		Menu menu_5 = new Menu(packageManagerTable);
		packageManagerTable.setMenu(menu_5);
		
		MenuItem mntmInstall = new MenuItem(menu_5, SWT.NONE);
		mntmInstall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				installApk();
				packageManagerController.loadPackages();
			}
		});
		mntmInstall.setText("Install APK");
		
		MenuItem mntmUninstall = new MenuItem(menu_5, SWT.NONE);
		mntmUninstall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setStatus("UnInstalling package: " + packageManagerTable.getSelection()[0].getText(0));
				packageManagerController.uninstallPackages(packageManagerTable.getSelection());
				clearStatus();
				packageManagerController.loadPackages();
			}
		});
		mntmUninstall.setText("Uninstall Package(s)");
		
		MenuItem mntmPullPackages = new MenuItem(menu_5, SWT.NONE);
		mntmPullPackages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String dir = GuiWorkshop.selectDirectory(shlOterTool);
				if(dir != null){
					packageManagerController.pullPackages(dir);
				}
			}
		});
		mntmPullPackages.setText("Pull Package(s)");
		
		new MenuItem(menu_5, SWT.SEPARATOR);
		
		MenuItem mntmRefreshList = new MenuItem(menu_5, SWT.NONE);
		mntmRefreshList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				packageManagerController.loadPackages();
			}
		});
		mntmRefreshList.setText("Refresh List");
		
		packageManagerStyledText = new StyledText(packageManagerSashForm, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		packageManagerSashForm.setWeights(new int[] {1, 3});
		FormData fd_statusLabel = new FormData();
		fd_statusLabel.bottom = new FormAttachment(100, -2);
		fd_statusLabel.left = new FormAttachment(tabFolder, 0, SWT.LEFT);
		fd_statusLabel.right = new FormAttachment(100);
		statusLabel.setLayoutData(fd_statusLabel);
		statusLabel.setText("Welcome");
	}
	
	private void installApk(){
		setStatus("Installing APK.");
		String file = GuiWorkshop.selectFile(shlOterTool, new String[]{"*.apk"});		
		if(file != null){
			this.display.asyncExec(new InstallApk(file));
		}
	}
	
	private class InstallApk implements Runnable{
		private String file;
		public InstallApk(String file){
			this.file = file;
		}
		public void run() {
			setStatus("Installing APK: " + file);
			try{
				AdbWorkshop.installApk(file);
				GuiWorkshop.messageDialog(shlOterTool, "The APK has been sucessfully installed");
			} catch (Exception e){
				GuiWorkshop.messageError(shlOterTool, "Could not install APK: " + e.getMessage());
				logger.error("Could not install APK:", e);
			}
			clearStatus();
		}
	}
	
	public void runRunnableAsync(Runnable r){
		this.display.asyncExec(r);
	}
	
	public void messageError(String error){
		this.display.asyncExec(new MessageErrorRunnable(shlOterTool, error));
	}
	private class MessageErrorRunnable implements Runnable{
		private Shell shlOterTool;
		private String error;
		public MessageErrorRunnable(Shell shell, String error){
			this.shlOterTool = shell;
			this.error = error;
		}
		public void run(){
			GuiWorkshop.messageError(shlOterTool, error);
		}
	}
	
	public void setSashFormWeights(SashForm sash, int[] weights){
		this.getDisplay().asyncExec(new SetSashFormWeights(sash, weights));
	}
	private class SetSashFormWeights implements Runnable{
		private SashForm sash;
		private int[] weights;
		public SetSashFormWeights(SashForm sash, int[] weights){
			this.sash = sash;
			this.weights = weights;
		}
		public void run(){
			sash.setWeights(weights);
		}
	}
	
	public void setStatus(final String status){
		this.display.asyncExec(new Runnable(){
			public void run() {
				statusLabel.setText(status);
				statusLabel.update();
				statusLabel.redraw();
			}
		});
	}

	public void setStatusBlocking(final String status){
		statusLabel.setText(status);
		statusLabel.update();
		statusLabel.redraw();
	}

	public void clearStatus(){
		setStatus("");
	}
	
	public class SetStatus implements Runnable{
		private String status;
		public SetStatus(String status){
			this.status = status;
		}
		public void run() {
			getDisplay().asyncExec(new Runnable(){
				public void run() {
					statusLabel.setText(status);
				}
			});
		}
	}

	public Table getLogcatTable() {
		return logcatTable;
	}
	public Display getDisplay() {
		return display;
	}
	public Button getLogcatCheckAutoscroll() {
		return logcatCheckAutoscroll;
	}

	public Button getLogcatCheckDebug() {
		return logcatCheckDebug;
	}

	public Button getLogcatCheckInfo() {
		return logcatCheckInfo;
	}

	public Button getLogcatCheckWarn() {
		return logcatCheckWarn;
	}

	public Button getLogcatCheckError() {
		return logcatCheckError;
	}

	public Button getLogcatCheckVerbose() {
		return logcatCheckVerbose;
	}

	public Text getLogcatTextFilter() {
		return logcatTextFilter;
	}

	public Label getStatusLabel() {
		return statusLabel;
	}

	public Tree getFsDiffFirstTree() {
		return fsDiffFirstTree;
	}
	
	public Tree getFsDiffSecondTree() {
		return fsDiffSecondTree;
	}

	public Tree getFsDifferencesTree() {
		return fsDifferencesTree;
	}

	public Shell getShell() {
		return shlOterTool;
	}

	public SashForm getFsDiffSashForm() {
		return fsDiffSashForm;
	}

	public Tree getSmaliTree() {
		return smaliTree;
	}

	public CTabFolder getSmaliTabFolder() {
		return smaliTabFolder;
	}

	public StyledText getPackageManagerStyledText() {
		return packageManagerStyledText;
	}

	public Table getPackageManagerTable() {
		return packageManagerTable;
	}

	public Text getSmaliSearchText() {
		return smaliSearchText;
	}

	public Table getSmaliSearchTable() {
		return smaliSearchTable;
	}

	public Button getSmaliSearchIgnoreCase() {
		return smaliSearchIgnoreCase;
	}

	public Button getSmaliSearchRegex() {
		return smaliSearchRegex;
	}
}
