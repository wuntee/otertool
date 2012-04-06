package com.wuntee.oter.view.widgets.runnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.StyledText;

public class FileToStyledTextRunnable implements Runnable {
	private static Logger logger = Logger.getLogger(FileToStyledTextRunnable.class);
	private File f;
	private StyledText styledText;
	
	public FileToStyledTextRunnable(File f, StyledText styledText) {
		this.f = f;
		this.styledText = styledText;
	}

	@Override
	public void run() {
		logger.debug("Loading file into styled text (" + f.getAbsolutePath() + ")");
		try{
			BufferedReader in = new BufferedReader(new FileReader(f));
			char[] buf = new char[2048];
			String file = "";
			while(in.ready()){
				in.read(buf);
				file = file + new String(buf);
			}
	
			styledText.setText(file);
			logger.debug("Done loading file");
		} catch(Exception e){
			logger.error("Could not load file:", e);
			styledText.setText(e.getMessage());
		}
	}

}
