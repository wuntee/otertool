package com.wuntee.oter.fs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.adb.AdbShell;
import com.wuntee.oter.exception.CommandFailedException;

public class FsWorkshop {
	private static Logger logger = Logger.getLogger(FsNode.class);
	
	public static List<FsNode> getFilesystem() throws Exception{
		AdbShell shell = new AdbShell();
		shell.execute();
		return(listDirectoryRecursive("/", shell));
	}
	
	public static List<FsNode> getDirectoryRecursive(String directory) throws Exception{
		AdbShell shell = new AdbShell();		
		shell.execute();
		try{
			shell.getRootShell();
		} catch(Exception e){
			// Ignore
		}
		return(listDirectoryRecursive(directory, shell));
	}
	
	private static List<FsNode> listDirectoryRecursive(String root, AdbShell shell) throws IOException, CommandFailedException{
		logger.debug("listDirectoryRecursive: " + root);
		List<FsNode> ret = new LinkedList<FsNode>();
		
		List<String> lines = shell.sendCommand("ls -l " + root);
		for(String line : lines){
			FsNode node = FsNode.getNode(line, root);
			if(!isBadDirectory(node.getFullPath())){
				if(node.getType().equals("d")){
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
	
	public static boolean isBadDirectory(String dir){
		logger.debug("Is bad directory: " + dir);
		for(String bad : OterStatics.BAD_DIRECTORIES){
			if(dir.equals(bad)){
				logger.debug("Yes");
				return(true);
			}
		}
		logger.debug("No");
		return(false);
	}
}
