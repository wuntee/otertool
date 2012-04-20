package com.wuntee.oter.aapt.androidmanifest;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class AndroidManifestObject {
	private static Logger logger = Logger.getLogger(AndroidManifestObject.class);

	private String name;
	private AndroidManifestObject parent;
	private List<AndroidManifestObject> children;
	
	public AndroidManifestObject(){
		name = "";
		children = new LinkedList<AndroidManifestObject>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AndroidManifestObject getParent() {
		return parent;
	}

	public void setParent(AndroidManifestObject parent) {
		this.parent = parent;
	}

	public List<AndroidManifestObject> getChildren() {
		return children;
	}

	public void setChildren(List<AndroidManifestObject> children) {
		this.children = children;
	}
	
	public void addChild(AndroidManifestObject child){
		this.children.add(child);
	}
}
