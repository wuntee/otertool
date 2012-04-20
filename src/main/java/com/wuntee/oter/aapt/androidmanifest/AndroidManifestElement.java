package com.wuntee.oter.aapt.androidmanifest;

public class AndroidManifestElement extends AndroidManifestObject {
	private int line = -1;

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	public static AndroidManifestElement parse(String line){
		//  E: manifest (line=2)
		AndroidManifestElement ret = new AndroidManifestElement();
		line = line.trim().substring(3);
		ret.setName(line.split(" ")[0]);
		String lineNumber = line.substring(line.lastIndexOf('=')+1, line.length()-1);
		ret.setLine(Integer.parseInt(lineNumber));
		return(ret);
	}
	
	public String toString(){
		return("AndroidManifestElement:name[" + this.getName() + "], line[" + this.getLine() + "]");
	}

}
