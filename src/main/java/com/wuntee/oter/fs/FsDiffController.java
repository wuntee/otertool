package com.wuntee.oter.fs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.adb.AdbShell;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.widgets.runnable.FsListToTreeRunnable;

public class FsDiffController {
	private static Logger logger = Logger.getLogger(FsDiffController.class);
	private Gui gui;
	
	private List<FsNode> first;
	private List<FsNode> second;
	
	public FsDiffController(Gui gui){
		this.gui = gui;
	}

	public void scanFirst() throws Exception{
		this.gui.getFsDiffSashForm().setWeights(new int[] {1, 1, 1});
		this.gui.getFsDiffFirstTree().removeAll();
		this.gui.getFsDiffSecondTree().removeAll();
		this.gui.getFsDifferencesTree().removeAll();
		Thread first = new Thread(new FirstFsScanRunnable(this.gui.getFsDiffFirstTree()));
		first.start();
	}
	
	public void scanSecond(){
		this.gui.getFsDiffSecondTree().removeAll();
		Thread second = new Thread(new SecondFsScanRunnable(this.gui.getFsDiffSecondTree()));
		second.start();
	}
	
	public void generateDifferences(){
		this.gui.setStatus("FsDiff: Generating differences");
		
		logger.debug("Flattening first.");
		List<FsNode> flatFirst = new LinkedList<FsNode>();
		for(FsNode node : first)
			flatFirst.addAll(flattenFsNode(node));
		
		logger.debug("Flattening second.");
		List<FsNode> flatSecond = new LinkedList<FsNode>();
		for(FsNode node : second)
			flatSecond.addAll(flattenFsNode(node));
		
		logger.debug("Generating differences");
		List<FsNode> newNodes = new LinkedList<FsNode>();
		List<List<FsNode>> updatedNodes = new LinkedList<List<FsNode>>();
		List<FsNode> deletedNodes = new LinkedList<FsNode>();
		
		// New
		for(FsNode secondNode : flatSecond){
			if(getNodeInList(secondNode, flatFirst) == null){
				newNodes.add(secondNode);
			}
		}
		logger.debug("New: " + newNodes.size());
		
		// Updated
		for(FsNode firstNode : flatFirst){
			FsNode secondNode = getNodeInList(firstNode, flatSecond);
			if(secondNode != null && !secondNode.equals(firstNode)){
				List<FsNode> update = new LinkedList<FsNode>();
				update.add(firstNode);
				update.add(secondNode);
				updatedNodes.add(update);
			}
		}
		logger.debug("Updated: " + updatedNodes.size());
		
		// Deleted
		for(FsNode firstNode : flatFirst){
			if(getNodeInList(firstNode, flatSecond) == null){
				deletedNodes.add(firstNode);
			}
		}
		logger.debug("Deleted: " + deletedNodes.size());
		
		// Add to list
		Tree tree = this.gui.getFsDifferencesTree();
		this.gui.getDisplay().asyncExec(new ListToDiffTree(tree, newNodes, updatedNodes, deletedNodes));
		
		this.gui.setSashFormWeights(this.gui.getFsDiffSashForm(), new int[] {1, 1, 6});
		
		this.gui.setStatus("");
	}
	
	public class ListToDiffTree implements Runnable{
		private Tree tree;
		private List<FsNode> newNodes;
		private List<List<FsNode>> updatedNodes;
		private List<FsNode> deletedNodes;
		public ListToDiffTree(Tree tree, List<FsNode> newNodes, List<List<FsNode>> updatedNodes, List<FsNode> deletedNodes){
			this.tree = tree;
			this.newNodes = newNodes;
			this.updatedNodes = updatedNodes;
			this.deletedNodes = deletedNodes;
		}
		public void run() {
			for(FsNode node : newNodes){
				TreeItem trtmTest = new TreeItem(tree, SWT.NONE);
				trtmTest.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
				if(node.isLink()){
					trtmTest.setText(new String[]{node.getFullPath() + " -> " + node.getLink(), node.getPermissions(), node.getGroup(), node.getUser(), (node.getSize() == -1 ? "" : String.valueOf(node.getSize())), node.getDate()});
				} else {
					trtmTest.setText(new String[]{node.getFullPath(), node.getPermissions(), node.getGroup(), node.getUser(), (node.getSize() == -1 ? "" : String.valueOf(node.getSize())), node.getDate()});
				}
				if(node.isDirectory()){
					trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
				} else{
					trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
				}
			}
			for(List<FsNode> nodes : updatedNodes){
				FsNode oldNode = nodes.get(0);
				FsNode newNode = nodes.get(1);
				TreeItem trtmTest = new TreeItem(tree, SWT.NONE);
				trtmTest.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
				if(oldNode.isLink()) {
					trtmTest.setText(oldNode.getFullPath() + " -> " + oldNode.getLink());
				} else {
					trtmTest.setText(oldNode.getFullPath());
				}
				if(oldNode.isDirectory()){
					trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_DIRECTORY));
				} else{
					trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
				}
				
				TreeItem oldTreeItem = new TreeItem(trtmTest, SWT.NONE);
				oldTreeItem.setText(new String[]{"First", oldNode.getPermissions(), oldNode.getGroup(), oldNode.getUser(), (oldNode.getSize() == -1 ? "" : String.valueOf(oldNode.getSize())), oldNode.getDate()});
				
				TreeItem newTreeItem = new TreeItem(trtmTest, SWT.NONE);
				newTreeItem.setText(new String[]{"Second", newNode.getPermissions(), newNode.getGroup(), newNode.getUser(), (newNode.getSize() == -1 ? "" : String.valueOf(newNode.getSize())), newNode.getDate()});
			}
			for(FsNode node : deletedNodes){
				TreeItem trtmTest = new TreeItem(tree, SWT.NONE);
				trtmTest.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				if(node.isLink()){
					trtmTest.setText(new String[]{node.getFullPath() + " -> " + node.getLink(), node.getPermissions(), node.getGroup(), node.getUser(), (node.getSize() == -1 ? "" : String.valueOf(node.getSize())), node.getDate()});
				} else {
					trtmTest.setText(new String[]{node.getFullPath(), node.getPermissions(), node.getGroup(), node.getUser(), (node.getSize() == -1 ? "" : String.valueOf(node.getSize())), node.getDate()});
				}
				if(node.isDirectory()){
					trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
				} else{
					trtmTest.setImage(SWTResourceManager.getImage(Gui.class, OterStatics.ICON_FILE));
				}
			}
		}
		
	}
	
	private FsNode getNodeInList(FsNode node, List<FsNode> list){
		for(FsNode scanNode : list)
			if(node.getFullPath().equals(scanNode.getFullPath()))
				return(scanNode);
		return(null);
	}
		
	private List<FsNode> flattenFsNode(FsNode source){
		List<FsNode> ret = new LinkedList<FsNode>();
		ret.add(source);
		if(source.isDirectory()){
			for(FsNode node : source.getChildren()){
				ret.addAll(flattenFsNode(node));
			}
		}
		return(ret);
	}
	
	public List<FsNode> getFilesystem() throws Exception{
		AdbShell shell = new AdbShell();
		shell.execute();
		shell.getRootShell();
		return(listDirectoryRecursive("/", shell));
	}
	
	private List<FsNode> listDirectoryRecursive(String root, AdbShell shell) throws IOException, CommandFailedException{
		logger.debug("listDirectoryRecursive: " + root);
		List<FsNode> ret = new LinkedList<FsNode>();
		
		List<String> lines = shell.sendCommand("ls -l " + root);
		for(String line : lines){
			FsNode node = FsNode.getNode(line, root);
			if(!FsWorkshop.isBadDirectory(node.getFullPath())){
				if(node.getType().equals("d")){
					gui.setStatus("FsDiff: Scanning " + node.getFullPath());
					logger.debug("Got directory, recursing: " + node.getFullPath());
					List<FsNode> secRet = listDirectoryRecursive(node.getFullPath(), shell);
					for(FsNode sec : secRet){
						node.addChild(sec);
					}
				}
				logger.debug("Adding to return: " + node.getName() + " with children: " + node.getChildren().size());
				ret.add(node);
			}
		}
		
		return(ret);
	}
	
	private void setFsToTree(List<FsNode> fs, Tree tree){
		this.gui.getDisplay().asyncExec(new FsListToTreeRunnable(fs, tree));
	}

	public class FirstFsScanRunnable implements Runnable{
		private Tree tree;
		public FirstFsScanRunnable(Tree tree){
			this.tree = tree;
		}
		public void run() {
			try {
				first = getFilesystem();
				setFsToTree(first, tree);
			} catch (Exception e) {
				gui.messageError("Error scanning: " + e.getMessage());
				logger.error("Error scanning:", e);
			}
		}		
	}

	public class SecondFsScanRunnable implements Runnable{
		private Tree tree;
		public SecondFsScanRunnable(Tree tree){
			this.tree = tree;
		}
		public void run() {
			try {
				second = getFilesystem();
				setFsToTree(second, tree);
				generateDifferences();
			} catch (Exception e) {
				gui.messageError("Error scanning: " + e.getMessage());
				logger.error("Error scanning:", e);
			}
		}		
	}
}
