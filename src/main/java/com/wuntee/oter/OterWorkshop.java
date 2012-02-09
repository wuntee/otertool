package com.wuntee.oter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class OterWorkshop {
	private static Logger logger = Logger.getLogger(OterWorkshop.class);

	public static void addAndroidjarToClasspath() throws Exception{
		String androidHome = getProperty(OterStatics.PROPERTY_ANDROID_HOME);
		if(androidHome == null){
			throw new Exception("Please configure android SDK location in configuration first.");
		}
		
		Collection<File> files = FileUtils.listFiles(new File(androidHome), new String[]{"jar"}, true);
		for(File f : files){
			if(f.getAbsolutePath().endsWith("android.jar")){
				Properties prop = getProperties();
				String currentClasspath = prop.getProperty(OterStatics.PROPERTY_JAVATOSMALI_CLASSPATH);
				String newClasspath = "";
				if(currentClasspath.split(":").length == 0){
					newClasspath = f.getAbsolutePath(); 
				} else {
					newClasspath = currentClasspath + ":" + f.getAbsolutePath();
				}
				prop.setProperty(OterStatics.PROPERTY_JAVATOSMALI_CLASSPATH, newClasspath);
				writeProp(prop, new File(OterStatics.getConfigFileName()));
				return;
			}
		}
		
		throw new Exception("Could not find android.jar in: " + androidHome);
	}
	
	public static String getProperty(String key){
		return(getProperties().getProperty(key));
	}
	
	public static Properties getProperties(){
		Properties prop = new Properties();
		
		// Does properties file exist?
		File propFile = new File(OterStatics.getConfigFileName());
		if(propFile.exists() == false){
			// Does not exist, write default
			writeDefaultProp(propFile);
		} else {
			logger.info("Loading prop file: " + OterStatics.getConfigFileName());
			try {
				prop.load(new FileInputStream(OterStatics.getConfigFileName()));
			} catch (FileNotFoundException e) {
				// should never happen, log
				logger.error("File not found: " + e.getMessage());
			} catch (IOException e) {
				// Cant do much here, just return default
				logger.error("Couldnt read properties file");
				prop = getDefaultProp();
			}
			
			// If nothing is in the file, load/write default
			if(prop.isEmpty() || prop.get(OterStatics.PROPERTY_LOGCAT_MAXLINES) == null){
				prop = writeDefaultProp(propFile);
			}
		}
		return(prop);
	}
	
	private static Properties writeDefaultProp(File propFile){
		Properties prop = getDefaultProp();
		writeProp(prop, propFile);
		return(prop);
	}
	
	private static Properties writeProp(Properties prop, File propFile){
		try {
			prop.store(new FileOutputStream(propFile), null);
		} catch (Exception e) {
			logger.error("Could not write properties file:", e);
		}
		return(prop);
	}
	
	private static Properties getDefaultProp(){
		Properties prop = new Properties();
		prop.put(OterStatics.PROPERTY_LOGCAT_MAXLINES, "10000");
		return(prop);
	}
	
	public static File createTemporaryDirectory(String prefix)
			throws IOException {
		File tmp = File
				.createTempFile(prefix, Long.toString(System.nanoTime()));
		logger.debug("Creating temporary directory: " + tmp.getAbsolutePath());
		tmp.delete();
		tmp.mkdir();
		tmp.deleteOnExit();
		return (tmp);
	}

	public static File createDirectoryRecursive(String directory) throws IOException {
		File f = new File(FilenameUtils.separatorsToSystem(directory));
		FileUtils.forceMkdir(f);
		return (f);
	}

    public static void unzipArchive(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
        	logger.error("Error while extracting file " + archive, e);
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
        } else {
	        File outputFile = new File(outputDir, entry.getName());
	        if (!outputFile.getParentFile().exists()){
	            createDir(outputFile.getParentFile());
	        }
	
	        logger.debug("Extracting: " + entry);
	        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
	        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
	
	        try {
	            IOUtils.copy(inputStream, outputStream);
	        } finally {
	            outputStream.close();
	            inputStream.close();
	        }
        }
    }

    private static void createDir(File dir) {
        logger.debug("Creating dir "+dir.getName());
        dir.mkdirs();
    }
    
    public static void zipArchive(File destArchive, File baseDirectory) throws IOException{
    	
    	logger.debug("Creating zip file: " + destArchive + ", from base: " + baseDirectory);
    	ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destArchive)));
    	
    	addDirToZip(baseDirectory, baseDirectory, out);
    	
    	out.close();
    }
    
    private static void addDirToZip(File baseDirectory, File directory, ZipOutputStream zipOutputStream) throws IOException{
    	String[] files = directory.list();

    	for(String file : files){
    		String fullFilePath = directory.getAbsoluteFile() + System.getProperty("file.separator") + file;
    		File fullFilePathFileObj = new File(fullFilePath);
    		String zipFilePath = fullFilePath.substring(baseDirectory.getAbsolutePath().length() + 1);
    		logger.debug("Adding: " + zipFilePath);
    		
    		if(fullFilePathFileObj.isDirectory()){
    			addDirToZip(baseDirectory, fullFilePathFileObj, zipOutputStream);
    		} else {
    			addFileToZip(zipFilePath, fullFilePath, zipOutputStream);			
    		}
    	}    	
    }
    
    private static void addFileToZip(String zipFilePath, String fullFilePath, ZipOutputStream zipOutputStream) throws IOException{
    	int BUFFER_SIZE = 2048;
    	 
		ZipEntry entry = new ZipEntry(zipFilePath);
		zipOutputStream.putNextEntry(entry);
		
		FileInputStream fis = new FileInputStream(fullFilePath);
		BufferedInputStream origin = new BufferedInputStream(fis, BUFFER_SIZE);
		byte data[] = new byte[BUFFER_SIZE];
		
		int count;
		while((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
			zipOutputStream.write(data, 0, count);
		}
		origin.close();
    }
    
    public static String classPathToFilePath(String clazz){
    	return(clazz.replace(".", System.getProperty("file.separator")));
    }
    
}
