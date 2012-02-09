package com.wuntee.oter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class OterStatics {
	public static final String META_INF = "META-INF";
	
	public static String PROPERTY_FILE = "config.properties";
	public static String PROPERTY_ADB_ARGUMENTS = "adb.arguments";
	public static String PROPERTY_ANDROID_HOME = "android.home";
	public static String PROPERTY_JAVATOSMALI_CLASSPATH = "javatosmali.classpath";
	
	public static String ICON_FILE = "/file_obj.gif";
	public static String ICON_DIRECTORY = "/folder.gif";
	public static String ICON_PACKAGE = "/package_obj.gif";
	public static String ICON_APP = "/otertool.gif";
	
	public static String[] BAD_DIRECTORIES = new String[]{"/dev/", "/acct/uid/", "/proc/", "/cache/", "/sys/", "/system/lost+found/"};
	public static String[] ADB_SHELLS = new String[]{"$ ", "# "};
	public static String ADB_ROOT_SHELL = "# ";
	
	public static String ANDROID_CERT_FILE = "/system/etc/security/cacerts.bks";
	
	public static String CLASSES_DEX = "classes.dex";
	
	public static String SMALI_EXTENSION = ".smali";
	
	public static String TEMP_PREFIX = "aat";
	
	public static String SOME_STRING = "wuntee";
	
	public static String getAndroidHome(){
		return(System.getProperty(PROPERTY_ANDROID_HOME));
	}
	
	public static Properties getOterConfig() throws FileNotFoundException, IOException{
		Properties prop = new Properties();
		prop.load(new FileInputStream(getConfigFileName()));
		return(prop);
	}
	
	public static String getJavaToSmaliClasspath(){
		try{
			String ret = getOterConfig().getProperty(PROPERTY_JAVATOSMALI_CLASSPATH);
			return(ret);
		} catch(Exception e) {
			return(null);
		}
	}
	
	public static String getEmulatorCommand(){
		return(getAndroidHome() + System.getProperty("file.separator") + "tools" + System.getProperty("file.separator") + "emulator");
	}
	
	public static String getAdbCommand(){
		return(getAndroidHome() + System.getProperty("file.separator") + "platform-tools" + System.getProperty("file.separator") + "adb");
	}
	
	public static String getAndroidCommand(){
		return(getAndroidHome() + System.getProperty("file.separator") + "tools" + System.getProperty("file.separator") + "android");
	}
	
	public static String getAaptCommand(){
		return(getAndroidHome() + System.getProperty("file.separator") + "platform-tools" + System.getProperty("file.separator") + "aapt");
	}
	
	public static String getDxCommand(){
		return(getAndroidHome() + System.getProperty("file.separator") + "platform-tools" + System.getProperty("file.separator") + "dx");
	}
	
	public static String getConfigFileName(){
		return(System.getProperty("user.dir") + System.getProperty("file.separator") + OterStatics.PROPERTY_FILE);
	}
}
