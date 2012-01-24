package com.wuntee.oter.styler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class SmaliLineStyler implements LineStyleListener {
	private static Logger logger = Logger.getLogger(SmaliLineStyler.class);
	
	private Display display;
	
	private Map<String, Color> colorMap;
		
	public SmaliLineStyler(){
		this.display = Display.getDefault();
		
		colorMap = new HashMap<String, Color>();
		colorMap.put("quote", new Color(display, new RGB(0, 0, 255)));
		colorMap.put("comment", display.getSystemColor(SWT.COLOR_DARK_GREEN));
		colorMap.put("colon", new Color(display, new RGB(128, 0, 128)));
		colorMap.put("dot", display.getSystemColor(SWT.COLOR_DARK_CYAN));
		colorMap.put("register", display.getSystemColor(SWT.COLOR_DARK_RED));
		

	}
	
	public void lineGetStyle(LineStyleEvent event) {
		boolean midStyle = false;
		int start = 0;
		char terminator = ';';
		char lastChar = '~';

		
		List<StyleRange> styles = new LinkedList<StyleRange>();
		// styles.add(new StyleRange(event.lineOffset, event.lineText.length(), colorMap.get("comment"), null));
		StyleRange x = new StyleRange();

		StyleRange style = null;
		for(int i=0; i<event.lineText.length(); i++){
			char subj = event.lineText.charAt(i);
			
			if(midStyle == false){
				if(subj == '#'){
					logger.debug("Comment");
					style = new StyleRange();
					style.start = event.lineOffset + i;
					style.foreground = colorMap.get("comment");
					midStyle = true;
					start = i;
				} else if(subj == '"' || subj =='\''){
					logger.debug("Quote");
					style = new StyleRange();
					style.start = event.lineOffset + i;
					style.foreground = colorMap.get("quote");
					midStyle = true;
					start = i;
				} else if(subj == ':' && lastChar == ' '){
					logger.debug("Colon");
					style = new StyleRange();
					style.start = event.lineOffset + i;
					style.foreground = colorMap.get("colon");
					terminator = ' ';
					midStyle = true;
					start = i;
				} else if(subj == '.'){
					logger.debug("Dot");
					style = new StyleRange();
					style.start = event.lineOffset + i;
					style.foreground = colorMap.get("dot");
					terminator = ' ';
					midStyle = true;
					start = i;
				}
			} else {
				if(i == event.lineText.length()-1){
					style.length = event.lineText.length() - start;
					styles.add(style);
					logger.debug("Adding: start: " + style.start + ", length = " + style.length);
					midStyle = false;
					start = 0;
				} else {
					if(subj == ';'){
						//styles.add(style);
						//style = null;
						//midStyle = false;
					}else if(subj == '"' || subj == '\''){
						style.length = i - start;
						styles.add(style);
						logger.debug("Adding: start: " + style.start + ", length = " + style.length);
						midStyle = false;
						start = 0;
						terminator = ';';
					}else if(subj == '}' || subj == ','){
						style.length = i - start;
						styles.add(style);
						logger.debug("Adding: start: " + style.start + ", length = " + style.length);
						midStyle = false;
						start = 0;
						terminator = ';';
					}else if(subj == terminator){
						style.length = i - start;
						styles.add(style);
						logger.debug("Adding: start: " + style.start + ", length = " + style.length);
						midStyle = false;
						start = 0;	
						terminator = ';';
					}
				}
				
			}
			lastChar = subj;
		}
		if(style != null){
			styles.add(style);
		}
		
		event.styles = (StyleRange[]) styles.toArray(new StyleRange[0]);
	}
}
