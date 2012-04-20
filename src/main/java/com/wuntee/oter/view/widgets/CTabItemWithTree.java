package com.wuntee.oter.view.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Tree;

public class CTabItemWithTree extends CTabItem {
	private Tree tree;

	public CTabItemWithTree(CTabFolder parent, int style) {
		super(parent, style);
		this.tree = new Tree(parent, SWT.BORDER);
		this.setControl(tree);
				
		this.setData(Tree.class.getName(), this.tree);
		this.setControl(this.tree);
	}

	public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

}
