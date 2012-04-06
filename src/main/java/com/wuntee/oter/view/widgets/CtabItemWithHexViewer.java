package com.wuntee.oter.view.widgets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

public class CtabItemWithHexViewer {
	private CTabFolder parent;
	private StyledText counter;
	private StyledText hexContent;
	private StyledText binContent;
	private String name;
	private File f;
	private int style;
	
	public CtabItemWithHexViewer(CTabFolder parent, String name, File f, int style) throws IOException{
		this.parent = parent;
		this.name = name;
		this.f = f;
		this.style = style;
		
		createEditor();
		loadFile(f);
	}
	
	private void loadFile(File f) throws IOException{
		hexContent.setText("");
		
		FileInputStream fis = new FileInputStream(f);
		
		byte[] buffer = new byte[2048];
		int ctr = 0;
		StringBuffer hexContentStringBuffer = new StringBuffer();
		StringBuffer counterStringBuffer = new StringBuffer("000000");
		StringBuffer binContentStringBuffer = new StringBuffer();
		while(fis.read(buffer) != -1){
			for(byte c: buffer){
				ctr++;
				String hex = String.format("%02X", c);
                
				char ch = (char)c;
                if (ch < 32 || ch > 126) {
                	ch = '.';
                }
                	
				if(ctr % 8 == 0){
					hexContentStringBuffer.append(hex).append("\n");
					binContentStringBuffer.append(ch).append("\n");
					counterStringBuffer.append("\n").append(String.format("%06d", ctr));
				} else if (ctr % 4 == 0){
					hexContentStringBuffer.append(hex).append("  ");
					binContentStringBuffer.append(ch).append(" ");
				} else {
					hexContentStringBuffer.append(hex).append(" ");
					binContentStringBuffer.append(ch);
				}
			}
		}
		hexContent.setText(hexContentStringBuffer.toString());
		binContent.setText(binContentStringBuffer.toString());
		counter.setText(counterStringBuffer.toString());

	}
	
	private void createEditor(){		
		CTabItem tabItem = new CTabItem(parent, style);
		tabItem.setText(name);
		
		Composite composite = new Composite(parent, SWT.NONE);
		tabItem.setControl(composite);
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		composite.setLayout(gl_composite);
		
		counter = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		counter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		counter.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NORMAL));

		hexContent = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		hexContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		hexContent.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NORMAL));
		hexContent.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent arg0) {
				counter.setTopIndex(hexContent.getTopIndex());
			}
		});

		binContent = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY);
		binContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		binContent.setFont(SWTResourceManager.getFont("Courier New", 11, SWT.NORMAL));

	}
}
