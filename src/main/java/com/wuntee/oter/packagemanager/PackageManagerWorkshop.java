package com.wuntee.oter.packagemanager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.adb.AdbWorkshop;
import com.wuntee.oter.command.TerminatingCommand;
import com.wuntee.oter.exception.CommandFailedException;

public class PackageManagerWorkshop {
	private static Logger logger = Logger.getLogger(AdbWorkshop.class);

	public static List<String> getDetailedPackageInfo(File apk) throws IOException, InterruptedException, CommandFailedException{
		logger.debug("Running aapt on " + apk);
		
		// Run: aapt l -a [apk]
		TerminatingCommand cmd = new TerminatingCommand(new String[]{OterStatics.getAaptCommand(), "l", "-a", apk.getAbsolutePath()});
		int r = cmd.executeNoErrorMonitor();
		logger.debug("Command returned: " + r);
		
		return(cmd.getOutput());
	}
}
