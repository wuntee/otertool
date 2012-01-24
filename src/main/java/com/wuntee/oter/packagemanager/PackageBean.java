package com.wuntee.oter.packagemanager;

import com.wuntee.oter.exception.ParseException;

public class PackageBean {
	private String apk;
	private String clazz;
	public static PackageBean parse(String s) throws ParseException{
		//package:/system/app/GoogleServicesFramework.apk=com.google.android.gsf
		String[] sSplit = s.split("=");
		
		PackageBean b = new PackageBean();
		if(sSplit.length == 2){
			b.setClazz(sSplit[1]);
			b.setApk(sSplit[0].substring(sSplit[0].indexOf(':') + 1));
			return(b);
		} else {
			throw new ParseException("Could not parse PackageBean: '" + s + "'");
		}
	}
	public String getApk() {
		return apk;
	}
	public void setApk(String apk) {
		this.apk = apk;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
}
