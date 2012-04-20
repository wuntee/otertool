package com.wuntee.oter.aapt.androidmanifest;

public class AndroidManifestAttribute extends AndroidManifestObjectWithValue {
	private String raw;
	private String type;
	
	public AndroidManifestAttribute(){
		super();
		raw = "";
		type = "";
	}
	
	public static AndroidManifestAttribute parse(String line){
		//      A: android:minSdkVersion(0x0101020c)=(type 0x10)0xa
		//      A: android:name(0x01010003)="android.permission.INTERNET" (Raw: "android.permission.INTERNET")
		//      A: package="com.wuntee.hca" (Raw: "com.wuntee.hca")
		AndroidManifestAttribute ret = new AndroidManifestAttribute();
		line = line.trim().substring(3);
		String[] splitLine = line.split("=");
		if(splitLine[0].indexOf("(") != -1){
			ret.setName(splitLine[0].substring(0, splitLine[0].indexOf("(")));
		} else {
			ret.setName(splitLine[0]);
		}
		
		if(splitLine[1].startsWith("(")){
			ret.setType(splitLine[1].substring(splitLine[1].indexOf(" ")+1, splitLine[1].indexOf(")")));
			ret.setValue(splitLine[1].substring(splitLine[1].indexOf(")")+1));
		} else if(splitLine[1].startsWith("\"")){
			//"android.permission.INTERNET" (Raw: "android.permission.INTERNET")
			String[] valueSplit = splitLine[1].split(" \\(");
			ret.setValue(valueSplit[0].trim().replaceAll("\"", ""));
			ret.setRaw(valueSplit[1].substring(6, valueSplit[1].length()-2));
		} else {
			ret.setValue(splitLine[1]);
		}
		return(ret);
	}
	
	public String toString(){
		return("AndroidManifestAttribute:name[" + this.getName() + "], value[" + this.getValue() + "], raw[" + this.getRaw() + "], type[" + this.getType() + "]");
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
