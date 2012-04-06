package com.wuntee.oter.view.widgets.runnable;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.fs.FsNode;
import com.wuntee.oter.view.Gui;

public class FsListToTreeRunnable implements Runnable {
	private List<FsNode> fs;
	private Tree tree;
	
	public FsListToTreeRunnable(List<FsNode> fs, Tree tree){
		this.fs = fs;
		this.tree = tree;
	}
	public void run() {
		tree.removeAll();
		
		for(FsNode node : fs){
			TreeItem trtmTest = new TreeItem(tree, SWT.NONE);
			
			trtmTest.setData(FsNode.class.getName(), node);
			
			if(node.isLink())
				trtmTest.setText(node.getName() + " -> " + node.getLink());
			else
				trtmTest.setText(node.getName());
			if(node.isDirectory()){
				addDirectory(trtmTest, node);
				trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_DIRECTORY));
			} else{
				trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
			}
		}
	}
	
	private void addDirectory(TreeItem ti, FsNode node){
		for(FsNode child : node.getChildren()){
			TreeItem trtmTest = new TreeItem(ti, SWT.NONE);
			
			trtmTest.setData(FsNode.class.getName(), child);
			
			trtmTest.setText(child.getName());
			if(child.isDirectory()){
				trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_DIRECTORY));
				addDirectory(trtmTest, child);
			} else {
				trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
			}
		}
	}
}
