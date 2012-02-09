package com.wuntee.oter.javatosmali;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.StyledText;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.styler.SmaliLineStyler;
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
			smaliStyledText.setText("");
			try {
				smaliStyledText.setText(JavaToSmaliWorkshop.javaSourceToSmali(javaStyledText.getText(), OterWorkshop.getProperty(OterStatics.PROPERTY_JAVATOSMALI_CLASSPATH)));
				gui.clearStatus();
			} catch(CommandFailedException e){
				smaliStyledText.setText(e.getMessage());
				gui.setStatus("Java to Smali: Could not compile java source");
			} catch (Exception e) {
				gui.setStatus("Java to Smali: " + e.getMessage());
			}
		}
	}
}
