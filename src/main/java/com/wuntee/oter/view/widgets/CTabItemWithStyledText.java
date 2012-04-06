package com.wuntee.oter.view.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;

public class CTabItemWithStyledText{
	private CTabItem cTabItem;
	private StyledText styledText;

	public CTabItemWithStyledText(CTabFolder parent, String name, int style) {
		this.cTabItem = new CTabItem(parent, style);
		this.cTabItem.setText(name);
		this.styledText = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		this.cTabItem.setData(StyledText.class.getName(), this.styledText);
		this.styledText.setEditable(true);
		this.cTabItem.setControl(this.styledText);
		parent.setSelection(cTabItem);
	}
	public StyledText getStyledText() {
		return styledText;
	}
	public void setStyledText(StyledText styledText) {
		this.styledText = styledText;
	}
	public CTabItem getcTabItem() {
		return cTabItem;
	}
	public void setcTabItem(CTabItem cTabItem) {
		this.cTabItem = cTabItem;
	}
}
