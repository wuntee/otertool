package com.wuntee.oter.avd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.android.prefs.AndroidLocation;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.ISystemImage;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;
import com.wuntee.oter.OterStatics;
import com.wuntee.oter.command.BackgroundCommand;
import com.wuntee.oter.exception.GenericException;
import com.wuntee.oter.view.bean.CreateAvdBean;

public class AvdWorkshop {
	private static Logger logger = Logger.getLogger(AvdWorkshop.class);

	public static AvdInfo createAvd(CreateAvdBean bean) throws AndroidLocationException, IOException, GenericException{

		ISdkLog sdkLogger = AvdWorkshop.getAvdLogger();
		
		SdkManager sdkManager = SdkManager.createManager(OterStatics.getAndroidHome(), sdkLogger);
		AvdManager avdManager = new AvdManager(sdkManager, sdkLogger);
		
		File avdFolder = new File(AndroidLocation.getFolder() + "avd", bean.getName() + ".avd");
		
		//newAvdInfo = avdManager.createAvd(avdFolder, avdName, target, skin, this.mSdkCommandLine.getParamSdCard(), hardwareConfig, removePrevious, this.mSdkCommandLine.getFlagSnapshot(), this.mSdkLog);
		
		IAndroidTarget target = getAndroidTargetFromString(sdkManager, bean.getTarget());

		String abiType = target.getSystemImages()[0].getAbiType(); //ABI = Android Base Image ?
		
		// /Applications/android-sdk-macosx/system-images/android-15/armeabi-v7a//system.img
		// avdManager.           createAvd(avdFolder, avdName,     avdTarget, ABI, skin, sdCard, hadwareConfig, snapshot, force, false, logger)
		//AvdInfo ret = avdManager.createAvd(avdFolder, bean.getName(),           target,        null,        null,                     null,        false,        false, sdkLogger);
		//                       createAvd(File arg0, String arg1, IAndroidTarget arg2, String arg3, String arg4, Map<String, String> arg5, boolean arg6, boolean arg7, ISdkLog arg8)
		AvdInfo ret = avdManager.createAvd(avdFolder, bean.getName(), target, abiType, null, null, null, false, false, false, sdkLogger);
		
		if(ret == null){
			logger.error("There was an error createing AVD, the manager returned a null info object.");
			throw new GenericException("Could not create AVD for an unknown reason.");
		}
		
		if(bean.isPersistant() == true){
			makeAvdPersistant(ret);
		}
		
		return(ret);
	}
	
	public static boolean isAvdExist(String avd) throws AndroidLocationException{
		ISdkLog sdkLogger = AvdWorkshop.getAvdLogger();
		
		SdkManager sdkManager = SdkManager.createManager(OterStatics.getAndroidHome(), sdkLogger);
		AvdManager avdManager = new AvdManager(sdkManager, sdkLogger);
		
		for(AvdInfo i : avdManager.getAllAvds()){
			if(i.getName().equals(avd)){
				return(true);
			}
		}
		return(false);
	}
	
	public static void makeAvdPersistant(AvdInfo info) throws IOException{
		logger.debug("Makeing AVD: " + info.getName() + " persistant.");
		File avdPath = new File(info.getDataFolderPath());
				
		ISystemImage[] sysImages = null;
				
		// If target is a platform, copy parents system images first
		if(info.getTarget().isPlatform() == false){
			for(ISystemImage iSysImg : info.getTarget().getParent().getSystemImages()){
				File f = new File(iSysImg.getLocation().getAbsoluteFile() + System.getProperty("file.separator") + "system.img");
				FileUtils.copyFileToDirectory(f, avdPath);
			}
		}

		// Copy target system images to local directory
		sysImages = info.getTarget().getSystemImages();
		for(ISystemImage iSysImg : info.getTarget().getSystemImages()){
			File f = new File(iSysImg.getLocation().getAbsoluteFile() + System.getProperty("file.separator") + "system.img");
			FileUtils.copyFileToDirectory(f, avdPath);
		}

		// If system.img exists, it should be system-qemu.img (http://code.google.com/p/android/issues/detail?id=23684)
		File sysImg = new File(avdPath + System.getProperty("file.separator") + "system.img");
		if(sysImg.exists()){
			sysImg.renameTo(new File(avdPath + System.getProperty("file.separator") + "system-qemu.img"));
		}
	}
	
	private static void copyDirContentsToDir(String firstDir, String destDir) throws IOException{
		logger.debug("Copyting '" + firstDir + "*' to '" + destDir + "'");
		File src = new File(firstDir);
		File dst = new File(destDir);
		for(File x : src.listFiles()){
			FileUtils.copyFileToDirectory(x, dst);
		}
	}
		
	public static IAndroidTarget getAndroidTargetFromString(SdkManager sdkManager, String target){
		for(IAndroidTarget t : sdkManager.getTargets()){
			if(t.hashString().equals(target)){
				return(t);
			}
		}
		return(null);
	}
	
	public static void launchAvd(AvdInfo avd) throws Exception{
		String avdName = "@" + avd.getName();
		BackgroundCommand c = new BackgroundCommand(new String[]{OterStatics.getEmulatorCommand(), "-partition-size", "1024", avdName});
		c.execute();
	}
	
	public static void launchPersistantAvd(AvdInfo avd) throws Exception{
		String avdName = "@" + avd.getName();
		String sysImage = avd.getDataFolderPath() + System.getProperty("file.separator") + "system-qemu.img";
		BackgroundCommand c = new BackgroundCommand(new String[]{OterStatics.getEmulatorCommand(), "-system", sysImage,  "-partition-size", "1024", avdName});
		c.execute();
	}
	
	public static ISdkLog getAvdLogger(){
		ISdkLog sdkLogger = new ISdkLog() {
			public void error(Throwable t, String errorFormat, Object[] args) {
				logger.error("Error: ", t);
			}

			public void warning(String warningFormat, Object[] args) {
				logger.warn(args);
			}

			public void printf(String msgFormat, Object[] args) {
				logger.debug(args);
			}
		};
		return(sdkLogger);
	}
}
