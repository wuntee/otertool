package com.wuntee.oter.aapt.androidmanifest;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.command.TerminatingCommand;
import com.wuntee.oter.exception.CommandFailedException;

public class AndroidManifestWorkshop {
	private static Logger logger = Logger.getLogger(AndroidManifestWorkshop.class);
	
	public static void printTree(AndroidManifestObject o){
		printRecur(o, "");
	}
	
	private static void printRecur(AndroidManifestObject o, String prefix){
		logger.debug(prefix + o);
		if(o.getChildren().size() > 0){
			for(AndroidManifestObject child : o.getChildren()){
				printRecur(child, prefix + "  ");
			}
		}
	}

	public static AndroidManifestObject getAndroidManifestObjectsForApk(File apkFile) throws CommandFailedException, InterruptedException, IOException{
		AndroidManifestObject root = null;
		
		// Run: aapt d xmltree [apk] AndroidManifest.xml
		TerminatingCommand cmd = new TerminatingCommand(new String[]{OterStatics.getAaptCommand(), "d", "xmltree", apkFile.getAbsolutePath(), "AndroidManifest.xml"});
		int r = cmd.executeNoErrorMonitor();
		logger.debug("Command returned: " + r);
		
		AndroidManifestObject lastObject = null;
		int lastDepth = 0;
		int currentDepth = 0;
		for(String line : cmd.getOutput()){
			logger.debug(line);
			while(line.charAt(currentDepth) == ' '){
				currentDepth++;
			}
			currentDepth = currentDepth/2;
					
			String trimLine = line.trim();
			AndroidManifestObject o = null;
			if(trimLine.startsWith("E:")){
				o = AndroidManifestElement.parse(trimLine);
			} else if(trimLine.startsWith("A:")){
				o = AndroidManifestAttribute.parse(trimLine);
			} else if(trimLine.startsWith("N:")){
				o = AndroidManifestNamespace.parse(trimLine);
			}

			logger.debug(o.toString());
			
			// Were the root
			if(currentDepth == 0){
				root = o;
			} else if(currentDepth > lastDepth){
				// new element is previous elements child
				o.setParent(lastObject);
				lastObject.addChild(o);
			} else if(currentDepth < lastDepth){
				int diff = lastDepth - currentDepth;
				AndroidManifestObject peer = lastObject;
				for(int i=0; i<diff; i++){
					peer = peer.getParent();
				}
				peer.getParent().addChild(o);
				o.setParent(peer.getParent());
			} else if(currentDepth == lastDepth){
				o.setParent(lastObject.getParent());
				lastObject.getParent().addChild(o);
			}
			lastObject = o;
			lastDepth = currentDepth;
		}

		return(root);
	}
}
