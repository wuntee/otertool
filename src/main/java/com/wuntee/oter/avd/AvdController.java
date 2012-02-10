package com.wuntee.oter.avd;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.internal.avd.AvdInfo;
import com.wuntee.oter.view.Gui;
import com.wuntee.oter.view.GuiWorkshop;
import com.wuntee.oter.view.bean.CreateAvdBean;

public class AvdController {
	private static Logger logger = Logger.getLogger(AvdController.class);

	private Gui gui;
	
	public AvdController(Gui gui){
		this.gui = gui;
	}
	
	public void createAvd(CreateAvdBean bean){
		try {
			AvdInfo avd = AvdWorkshop.createAvd(bean);
			if(bean.isLaunch()){
				if(bean.isPersistant()){
					AvdWorkshop.launchPersistantAvd(avd);
				} else {
					AvdWorkshop.launchAvd(avd);
				}
			} else {
				GuiWorkshop.messageDialog(gui.getShell(), "The AVD '" + bean.getName() + "' has sucessfully been created!");
			}
		} catch (AndroidLocationException e) {
			GuiWorkshop.messageError(gui.getShell(), "Could not create AVD: " + e.getMessage());
			logger.error("Could not create AVD:", e);
		} catch(IOException e) {
			GuiWorkshop.messageError(gui.getShell(), "Could not copy base image files to AVD directory: " + e.getMessage());
			logger.error("Could not copy base image files to AVD directory: ", e);
		} catch (Exception e) {
			GuiWorkshop.messageError(gui.getShell(), "Could not launch the emulator: " + e.getMessage());
			logger.error("Could not launch the emulator", e);
		}
	}

}
