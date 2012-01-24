package com.wuntee.oter.view.bean;

public class BuildAndSignApkBean {
	private String apkFilename;
	private String certFilename;
	private String password;
	private String certAlias;
	private boolean sign;
	
	public String getApkFilename() {
		return apkFilename;
	}
	public void setApkFilename(String apkFilename) {
		this.apkFilename = apkFilename;
	}
	public String getCertFilename() {
		return certFilename;
	}
	public void setCertFilename(String certFilename) {
		this.certFilename = certFilename;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCertAlias() {
		return certAlias;
	}
	public void setCertAlias(String certAlias) {
		this.certAlias = certAlias;
	}
	public boolean isSign() {
		return sign;
	}
	public void setSign(boolean sign) {
		this.sign = sign;
	}
	
}
