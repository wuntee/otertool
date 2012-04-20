package com.wuntee.oter.view.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.wuntee.oter.aapt.androidmanifest.AndroidManifestAttribute;
import com.wuntee.oter.aapt.androidmanifest.AndroidManifestElement;
import com.wuntee.oter.aapt.androidmanifest.AndroidManifestNamespace;
import com.wuntee.oter.aapt.androidmanifest.AndroidManifestObject;

public class CTabItemWithTreeForAndroidManifest extends CTabItemWithTree {

	public CTabItemWithTreeForAndroidManifest(CTabFolder parent, int style) {
		super(parent, style);
	}

	public void loadAndroidManifestObjects(AndroidManifestObject root){
		this.getTree().removeAll();
		loadRecur(root, null);
		
	}
	
	private void loadRecur(AndroidManifestObject o, TreeItem parent){
		TreeItem ti = null;
		if(parent == null){
			ti = new TreeItem(this.getTree(), SWT.NONE);
		} else {
			ti = new TreeItem(parent, SWT.NONE);
		}
		if(o instanceof AndroidManifestElement){
			AndroidManifestElement e = (AndroidManifestElement)o;
			ti.setText(e.getName());
			if(e.getName().equalsIgnoreCase("uses-permission")){
				ti.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			} else if(e.getName().equalsIgnoreCase("activity")){
				ti.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
			} else if(e.getName().equalsIgnoreCase("receiver") || e.getName().equalsIgnoreCase("provider") ){
				ti.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			}
		} else if(o instanceof AndroidManifestNamespace){
			AndroidManifestNamespace n = (AndroidManifestNamespace)o;
			ti.setText(n.getName() + "=" + n.getValue());
		} else if(o instanceof AndroidManifestAttribute){
			AndroidManifestAttribute a = (AndroidManifestAttribute)o;
			if(a.getRaw().equals("") && a.getType().equals("")){
				ti.setText(a.getName() + "=" + a.getValue());
			} else if(a.getRaw().equals("") && !a.getType().equals("")){
				ti.setText(a.getName() + "[" + a.getType() + "]=" + a.getValue());
			} else if(!a.getRaw().equals("") && a.getType().equals("")){
				ti.setText(a.getName() + "[" + a.getRaw() + "]=" + a.getValue());
			} else { 
				ti.setText(a.getName() + "[" + a.getType() + ":" + a.getRaw() + "]=" + a.getValue());
			}
		}
		String attributes = " [";
		for(AndroidManifestObject child : o.getChildren()){
			if(child instanceof AndroidManifestAttribute){
				AndroidManifestAttribute a = (AndroidManifestAttribute)child;
				attributes = attributes + a.getName() + "=" + a.getValue() + ", "; 
			} else {
				loadRecur(child, ti);
			}			
		}
		if(!attributes.equals(" [")){
			ti.setText(ti.getText() + attributes.substring(0, attributes.length()-2) + "]");
		}
		ti.setExpanded(true);

	}
}
