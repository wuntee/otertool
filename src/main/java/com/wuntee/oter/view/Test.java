package com.wuntee.oter.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.MouseEvent;

public class Test {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Test window = new Test();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("SWT Application");
		shell.setLayout(new GridLayout(1, false));
		
		CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
		tabFolder.setSimple(false);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText("New Item");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(composite);
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		composite.setLayout(gl_composite);
		
		StyledText styledText = new StyledText(composite, SWT.BORDER);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		StyledText styledText_1 = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		
		styledText_1.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NORMAL));
		styledText_1.setText("00 12 34 3f ff");
		styledText_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		StyledText styledText_2 = new StyledText(composite, SWT.BORDER);
		styledText_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
	}
}
