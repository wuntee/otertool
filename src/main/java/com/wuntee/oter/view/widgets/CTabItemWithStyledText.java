package com.wuntee.oter.view.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;

public class CTabItemWithStyledText extends CTabItem{
	private StyledText styledText;

	public CTabItemWithStyledText(CTabFolder parent, String name, int style) {
		this(parent, style);
		this.setText(name);
	}
	
	public CTabItemWithStyledText(CTabFolder parent, int style) {
		super(parent, style);
		this.styledText = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		this.setData(StyledText.class.getName(), this.styledText);
		this.styledText.setEditable(true);
		this.setControl(this.styledText);
		parent.setSelection(this);
	}
	public StyledText getStyledText() {
		return styledText;
	}
	public void setStyledText(StyledText styledText) {
		this.styledText = styledText;
	}
}
