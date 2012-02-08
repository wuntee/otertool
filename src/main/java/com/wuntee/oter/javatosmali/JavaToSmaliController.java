package com.wuntee.oter.javatosmali;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.StyledText;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.command.TerminatingCommand;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.exception.UnknownClassnameException;
import com.wuntee.oter.smali.SmaliWorkshop;
import com.wuntee.oter.view.Gui;

public class JavaToSmaliController {
	private static Logger logger = Logger.getLogger(JavaToSmaliController.class);
	private Gui gui;
		
	public JavaToSmaliController(Gui gui){
		this.gui = gui;
	}
	
	public void tryToCompileJava(StyledText javaStyledText, StyledText smaliStyledText){
		gui.setStatus("Java to Smali: Compiling java to smali");
		gui.runRunnableAsync(new TryToCompileJavaToSmali(javaStyledText, smaliStyledText));
	}
	
	public class TryToCompileJavaToSmali implements Runnable {
		private StyledText javaStyledText;
		private StyledText smaliStyledText;
		public TryToCompileJavaToSmali(StyledText javaStyledText, StyledText smaliStyledText){
			this.javaStyledText = javaStyledText;
			this.smaliStyledText = smaliStyledText;
		}
		public void run() {
			File javaSource = null;
			String className;
			String classFile;
			try {
				this.smaliStyledText.setText("");
				className = JavaToSmaliWorkshop.findClassNameFromJavaSource(javaStyledText.getText());
				classFile = className + ".dex";
				javaSource = JavaToSmaliWorkshop.writeJavaToFile(this.javaStyledText.getText());
			} catch (IOException e) {
				logger.error("Could not write java file: " + e.getMessage());
				return;
			} catch (UnknownClassnameException e) {
				gui.setStatus("Java to Smali: Could not determine the java source class name");
				logger.error("Could not determine the java source class name: " + e.getMessage());
				return;
			}

			try {
				logger.debug("Compiling java source.");
				TerminatingCommand javacCmd = new TerminatingCommand(new String[]{"javac", javaSource.getAbsolutePath()});
				javacCmd.execute();
					
			} catch(Exception e) {
				gui.setStatus("Java to Smali: Failed to compile");
				
				logger.debug("Failed to compile.");
				this.smaliStyledText.setText(e.getMessage());
				return;
			}
				
			try{
				logger.debug("Compiling class to dex.");
				TerminatingCommand dxCommand = new TerminatingCommand(new String[]{OterStatics.getDxCommand(), "--output=" + classFile, "--dex", className + ".class"}, javaSource.getParentFile());
				dxCommand.execute();
			} catch(Exception e) {
				gui.setStatus("Java to Smali: Could not compile dex file");
				
				logger.debug("Could not compile to dex file: " + e.getMessage());
				this.smaliStyledText.setText(e.getMessage());
				return;
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
				smaliStyledText.setText(buf);
				gui.setStatus("Java to Smali: Finished");
				
			} catch(Exception e) {
				gui.setStatus("Java to Smali: Could not decompile dex");
				
				logger.debug("Could not decompile to dex: " + e.getMessage());
				this.smaliStyledText.setText(e.getMessage());
				return;
			}

		}
		
	}
}
