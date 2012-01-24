package com.wuntee.oter.view;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class FindDialog extends Dialog {
	private static Logger logger = Logger.getLogger(FindDialog.class);

	protected Object result;
	protected Shell shlFind;
	private Text searchText;
	private StyledText searchContext;
	private Button btnIgnoreCase;

	
	/**
	 * @wbp.parser.constructor
	 */
	public FindDialog(Shell parent, StyledText searchContext) {
		// Pass the default styles here
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, searchContext);
	}

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FindDialog(Shell parent, int style, StyledText searchContext) {
		super(parent, style);
		setText("Search");
		this.searchContext = searchContext;
	}
	
	private void findNext(){
		String search = searchText.getText();
		
		logger.debug("Searching for: " + search);
		
		Point p = searchContext.getSelectionRange();
		String rest = searchContext.getTextRange(p.x+p.y, searchContext.getText().length()-(p.x+p.y));
		int x = rest.indexOf(search);
		
		if(x != -1){
			x =  p.x + p.y + x;
			int y = x + search.length();
			searchContext.setSelection(x, y);
		} else if(p.x != 0 && p.y != 0){
			searchContext.setSelection(0, 0);
			findNext();
		}
	}
	
	private void findPrevious(){
		String search = searchText.getText();
		
		logger.debug("Searching for: " + search);
		
		Point p = searchContext.getSelectionRange();
		String rest = searchContext.getTextRange(0, p.x);
		int x = rest.lastIndexOf(search);
		
		if(x != -1){
			int y = x + search.length();
			searchContext.setSelection(x, y);
		} else if(p.x != searchContext.getText().length() && p.y != 0){
			searchContext.setSelection(searchContext.getText().length(), searchContext.getText().length());
			findPrevious();
		}
	}

	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlFind.open();
		shlFind.layout();
		Display display = getParent().getDisplay();
		while (!shlFind.isDisposed()) {
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
		shlFind = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlFind.setSize(450, 128);
		shlFind.setText("Find");
		GridLayout gl_shlFind = new GridLayout(1, false);
		gl_shlFind.verticalSpacing = 0;
		gl_shlFind.marginWidth = 0;
		gl_shlFind.marginBottom = 5;
		gl_shlFind.marginTop = 5;
		gl_shlFind.marginRight = 5;
		gl_shlFind.marginLeft = 5;
		gl_shlFind.marginHeight = 0;
		gl_shlFind.horizontalSpacing = 0;
		shlFind.setLayout(gl_shlFind);
		
		Label lblSearchString = new Label(shlFind, SWT.NONE);
		lblSearchString.setBounds(0, 0, 59, 14);
		lblSearchString.setText("Search String:");
		
		searchText = new Text(shlFind, SWT.BORDER);
		searchText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.character == 13){
					findNext();
				}
			}
		});
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnIgnoreCase = new Button(shlFind, SWT.CHECK);
		btnIgnoreCase.setText("Ignore Case");
		
		Composite composite = new Composite(shlFind, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, true, 1, 1));
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		
		Button btnPrevious = new Button(composite, SWT.NONE);
		btnPrevious.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				findPrevious();
			}
		});
		btnPrevious.setText("Previous");
		
		Button btnNext = new Button(composite, SWT.NONE);
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				findNext();
			}
		});
		btnNext.setText("Next");
		
		Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shlFind.close();
			}
		});
		btnCancel.setBounds(0, 0, 94, 30);
		btnCancel.setText("Cancel");

	}
}
