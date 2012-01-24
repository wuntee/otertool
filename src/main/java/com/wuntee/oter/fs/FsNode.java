package com.wuntee.oter.fs;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class FsNode {
	private static Logger logger = Logger.getLogger(FsNode.class);
	
	private List<FsNode> children;
	private FsNode parent;
	
	private String permissions;
	private String user = "";
	private String group = "";
	private int size;
	private String date = "";
	private String name = "";
	private String link = "";
	private String type = "";
	private String raw = "";
	private String fullPath = "";
	private String root = "";
	
	public FsNode(String raw, FsNode root){
		this(raw, root.getName());
	}
	
	public FsNode(String raw, String root){
		children = new LinkedList<FsNode>();
		this.raw = raw;
		this.root = root;
	}
	
	public void parse(){
		logger.debug("Parsing:\t" + raw);
		
		this.type = raw.substring(0, 1);
		logger.debug("Type:\t\t" + type);
		
		this.permissions = raw.substring(1, 11);
		logger.debug("Perm:\t\t" + permissions);
		
		String[] split = raw.split("\\s+");
		this.user = split[1];
		logger.debug("User: '" + user + "'");
		this.group = split[2];
		logger.debug("Group: '" + group + "'");

		if(this.type.equals("-")){
			this.size = Integer.valueOf(split[3]);
			this.date = split[4] + " " + split[5];
			this.name = split[6];
			this.link = "";
		} else if(this.type.equals("d")){
			this.size = -1;
			this.date = split[3] + " " + split[4];
			if(split.length > 5){
				this.name = split[5];
			} else {
				this.name = " ";
			}
			this.link = "";
		} else if(this.type.equals("l")){
			this.size = -1;
			this.date = split[3] + " " + split[4];
			this.name = split[5];
			this.link = split[7];
		} else if(this.type.equals("c")){
			this.size = -1;
			this.date = split[5] + " " + split[6];
			this.name = split[7];
			this.link = "";
		}
		
		if(!this.root.endsWith("/")){
			this.root = this.root + "/";
		}
		
		if(this.type.equals("d")){
			this.fullPath = this.root + this.name + "/";
		} else {
			this.fullPath = this.root + this.name;
		}
		
		logger.debug("Size: '" + size + "'");
		logger.debug("Date: '" + date + "'");
		logger.debug("Name: '" + name + "'");
		logger.debug("Link: '" + link + "'");
		logger.debug("root: '" + root + "'");
		logger.debug("Full: '" + fullPath + "'");
		
	}
	
	@Override
	public boolean equals(Object obj){
		logger.debug("Equals?");
		FsNode n = null;
		try{
			n = (FsNode)obj;
		} catch (Exception e){
			logger.debug("Could not cast to FsNode - not the same.");
			return(false);
		}

		logger.debug("Differences:");
		logger.debug(this.fullPath.equals(n.getFullPath()) + " " + this.getFullPath() + " : " + n.getFullPath());
		logger.debug((this.size == n.getSize()) + " " + this.size + " : " + n.getSize());
		logger.debug(this.date.equals(n.getDate()) + " " + this.date + " : " + n.getDate());
		logger.debug(this.link.equals(n.getLink()) + " " + this.link + " : " + n.getLink());
		if(this.fullPath.equals(n.getFullPath()) &&
				this.size == n.getSize() && 
				this.date.equals(n.getDate()) &&
				this.link.equals(n.getLink()) ){
			logger.debug("Same");
			return(true);
		} else {
			return(false);
		}
	}
	
	public static FsNode getNode(String raw, String root){
		FsNode ret = new FsNode(raw, root);
		ret.parse();
		return(ret);
	}
	
	public static FsNode getNode(String raw, FsNode root){
		FsNode ret = new FsNode(raw, root);
		ret.parse();
		return(ret);
	}
	
	public boolean isDirectory(){
		return(this.type.equals("d"));
	}
	
	public boolean isLink(){
		return(this.type.equals("l"));
	}
	
	public void addChild(FsNode node){
		children.add(node);
	}
	
	public List<FsNode> getChildren(){
		return(children);
	}

	public FsNode getParent() {
		return parent;
	}

	public void setParent(FsNode parent) {
		this.parent = parent;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

}
