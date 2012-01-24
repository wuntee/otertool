package com.wuntee.oter.view.bean;

public class CreateAvdBean {
	private String name;
	private String target;
	private boolean persistant;
	private boolean launch;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public boolean isPersistant() {
		return persistant;
	}
	public void setPersistant(boolean persistant) {
		this.persistant = persistant;
	}
	public boolean isLaunch() {
		return launch;
	}
	public void setLaunch(boolean launch) {
		this.launch = launch;
	}
	
}
