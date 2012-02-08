package com.wuntee.oter.javatosmali;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.exception.UnknownClassnameException;

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
}
