package com.wuntee.oter.aapt.androidmanifest;

public class AndroidManifestObjectWithValue extends AndroidManifestObject {
	private String value;
	
	public AndroidManifestObjectWithValue(){
		super();
		value = "";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
