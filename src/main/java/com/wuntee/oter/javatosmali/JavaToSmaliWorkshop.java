package com.wuntee.oter.javatosmali;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.command.TerminatingCommand;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.exception.UnknownClassnameException;
import com.wuntee.oter.smali.SmaliWorkshop;

public class JavaToSmaliWorkshop {
	private static Logger logger = Logger.getLogger(JavaToSmaliController.class);

	public static boolean isValidJava(String java){
		return(true);
	}
		
	public static File writeJavaToFile(String javaSource) throws IOException, UnknownClassnameException{
		// Find the class name
		String className = findClassNameFromJavaSource(javaSource);
		if(className == null){
			logger.error("Could not determine class name.");
			throw new UnknownClassnameException();
		}
		
		// Write source to file
		File tempDir = OterWorkshop.createTemporaryDirectory("otertoolCompileJava");
		tempDir.deleteOnExit();
		File javaSourceFile = new File(tempDir, className + ".java");
		BufferedWriter out = new BufferedWriter(new FileWriter(javaSourceFile));
		out.write(javaSource);
		out.close();
		
		return(javaSourceFile);
	}
		
	public static String findClassNameFromJavaSource(String javaSource){
		javaSource = javaSource.replace("\n", " ");
		if(javaSource.matches(".*public\\s+class.*")){
			int startIndex = javaSource.indexOf("class") + "class".length();
			int endIndex = javaSource.indexOf("{");
			return(javaSource.substring(startIndex, endIndex).trim());
		}
		return(null);
	}
	
	public static String javaSourceToSmali(String javaSourceText, String classpathString) throws Exception{
		File javaSource;
		String className;
		String classFile;
		try {
			className = JavaToSmaliWorkshop.findClassNameFromJavaSource(javaSourceText);
			classFile = className + ".dex";
			javaSource = JavaToSmaliWorkshop.writeJavaToFile(javaSourceText);
		} catch (IOException e) {
			logger.error("Could not write java file: " + e.getMessage());
			throw new Exception("Could not write java file");
		} catch (UnknownClassnameException e) {
			logger.error("Could not determine the java source class name: " + e.getMessage());
			throw new Exception("Could not determine the java class name");
		}

		logger.debug("Compiling java source.");
		TerminatingCommand javacCmd = null;
		if(classpathString != null){
			javacCmd = new TerminatingCommand(new String[]{"javac", "-classpath", classpathString, javaSource.getAbsolutePath()});
		} else {
			javacCmd = new TerminatingCommand(new String[]{"javac", javaSource.getAbsolutePath()});
		}
		javacCmd.execute();
		
		try{
			logger.debug("Compiling class to dex.");
			TerminatingCommand dxCommand = new TerminatingCommand(new String[]{OterStatics.getDxCommand(), "--output=" + classFile, "--dex", className + ".class"}, javaSource.getParentFile());
			dxCommand.execute();
		} catch(Exception e) {			
			logger.debug("Could not compile to dex file: " + e.getMessage());
			throw new Exception("Could not compile dex file");
		}
			
		try{
			logger.debug("Decompiling dex to smali.");
			File dexFile = new File(javaSource.getParentFile() + System.getProperty("file.separator") + classFile);
			Map<String, File> smaliMap = SmaliWorkshop.getSmaliSource(dexFile, javaSource.getParentFile());
			logger.debug("Number of sources: " + smaliMap.size());
			File smaliSourceFile = (File)smaliMap.values().toArray()[0];
			BufferedReader in = new BufferedReader(new FileReader(smaliSourceFile));
			String buf = "";
			String str;
			while ((str = in.readLine()) != null) {
				buf = buf + str + "\n";
			}
			return(buf);
		} catch(Exception e) {			
			logger.debug("Could not decompile to dex: " + e.getMessage());
			throw new Exception("Could not decompile dex");
		}
	}
}
