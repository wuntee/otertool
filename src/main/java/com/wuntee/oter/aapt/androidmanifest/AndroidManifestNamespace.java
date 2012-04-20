package com.wuntee.oter.aapt.androidmanifest;

public class AndroidManifestNamespace extends AndroidManifestObjectWithValue {
	public static AndroidManifestNamespace parse(String line){
		//N: android=http://schemas.android.com/apk/res/android
		AndroidManifestNamespace ret = new AndroidManifestNamespace();
		line = line.trim().substring(3);
		String[] keyValue = line.split("=");
		ret.setName(keyValue[0]);
		ret.setValue(keyValue[1]);
		return(ret);
	}
	
	public String toString(){
		return("AndroidManifestAttribute:name[" + this.getName() + "], value[" + this.getValue() + "]");
	}

}
