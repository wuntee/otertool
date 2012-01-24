package com.wuntee.oter.adb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.command.TerminatingCommand;
import com.wuntee.oter.exception.AatException;
import com.wuntee.oter.exception.CommandFailedException;
import com.wuntee.oter.exception.NotRootedException;
import com.wuntee.oter.exception.ParseException;
import com.wuntee.oter.exception.UninstallException;
import com.wuntee.oter.packagemanager.PackageBean;

public class AdbWorkshop {
	private static Logger logger = Logger.getLogger(AdbWorkshop.class);
	
	public static boolean isConnected(){
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "shell", "ls"});
		try {
			int ret = c.execute();
			if(ret == 0){
				return(true);
			}
		} catch (Exception e) {
			logger.error("Could not execute isConnected command: ", e);
		}	
		return(false);
	}
	
	public static void installApk(String apk) throws IOException, InterruptedException, CommandFailedException{
		String[] cmd = new String[]{OterStatics.getAdbCommand(), "install", apk};
		TerminatingCommand c = new TerminatingCommand(cmd);
		c.execute();
		for(String l : c.getOutput()){
			// Error output: 
			// Failure [INSTALL_FAILED_ALREADY_EXISTS]
			if(l.matches(".*Failure.*")){
				throw new CommandFailedException(cmd, c.getOutput(), l);
			}
		}
	}
	
	public static void restartAdb() throws IOException, InterruptedException, CommandFailedException{
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "kill-server"});
		c.execute();
		c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "start-server"});
		c.execute();
	}
	
	public static File pullFileTo(String remoteFile, String localFile) throws IOException, InterruptedException, CommandFailedException{
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "pull", remoteFile, localFile});
		c.execute();
		return(new File(localFile));
		
	}
	
	public static File pullFile(String remoteFile) throws IOException, InterruptedException, CommandFailedException{
		File tmpFile = AdbWorkshop.getTemporaryFile();
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "pull", remoteFile, tmpFile.getAbsolutePath()});
		c.execute();
		return(tmpFile);
	}
	
	public static File pushFile(File localFile, String remotePath) throws IOException, InterruptedException, CommandFailedException{
		File tmpFile = AdbWorkshop.getTemporaryFile();
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "push", localFile.getAbsolutePath(), remotePath});
		c.execute();
		return(tmpFile);
	}
	
	public static void mountFilesystemReadWrite() throws IOException, InterruptedException, CommandFailedException, AatException {
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "shell", "mount", "-o", "remount,rw", AdbWorkshop.findSystemDirectory(), "/system"});
		c.execute();
	}
	
	public static void changeFilePermissions(String filename, String permissions) throws IOException, InterruptedException, CommandFailedException{
		TerminatingCommand c = new TerminatingCommand(new String[]{OterStatics.getAdbCommand(), "shell", "chmod", permissions, filename});
		c.execute();		
	}
	
	public static File getTemporaryFile() throws IOException{
		File tmp = File.createTempFile("aat.tmp", Long.toString(System.nanoTime()));
		tmp.delete();
		return(tmp);
	}
	
	public static boolean canGetRoot() throws Exception {
		AdbShell tmpShell = new AdbShell();
		tmpShell.start();
		try{
			tmpShell.getRootShell();
			return(true);
		} catch (Exception e) {
			return(false);
		} finally {
			tmpShell.close();
		}
	}
	
	public static List<String> runAdbCommand(String[] cmd) throws IOException, InterruptedException, CommandFailedException{
		String[] c = new String[cmd.length + 1];
		c[0] = OterStatics.getAdbCommand();
		for(int i=0; i<cmd.length; i++){
			c[i+1] = cmd[i];
		}
		TerminatingCommand tc = new TerminatingCommand(c);
		tc.execute();
		
		return(tc.getOutput());			
	}
	
	public static void installCert(File certfile, String password) throws NotRootedException, Exception{
		if(!AdbWorkshop.canGetRoot()){
			throw new NotRootedException();
		}
		
		Security.addProvider(new BouncyCastleProvider());

		// Pull the keystore, and load it into a Keystore object
		logger.debug("Pulling cacerts file");
		File cacerts = AdbWorkshop.pullFile(OterStatics.ANDROID_CERT_FILE);
		KeyStore ks = KeyStore.getInstance("BKS");
		FileInputStream fis = new java.io.FileInputStream(cacerts);
		ks.load(fis, password.toCharArray());
		
		// Read the certificate, and add it to the certfile
		logger.debug("Reading the cert, and adding it to a certfile: " + certfile.getName());
		FileInputStream is = new FileInputStream(certfile);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(is);
        ks.setCertificateEntry(certfile.getName(), cert);
		
		// Write cacert back out
        logger.debug("Writing temporary certfile to disk");
        File localFile = AdbWorkshop.getTemporaryFile();
		FileOutputStream fos = new java.io.FileOutputStream(localFile);
		ks.store(fos, password.toCharArray());
		
		// Mount FS as read/write
		logger.debug("Mounting remote filesystem rw");
		AdbWorkshop.mountFilesystemReadWrite();
		
		// Change file permissions of current cacert
		logger.debug("Changing permissions of certfile to 777");
		AdbWorkshop.changeFilePermissions(OterStatics.ANDROID_CERT_FILE, "777");
		
		// Push the new file back up
		logger.debug("Pushing the file on to the device");
		AdbWorkshop.pushFile(localFile, OterStatics.ANDROID_CERT_FILE);
		
		// Change file permissions back
		logger.debug("Changing permissions of certfile to 644");
		AdbWorkshop.changeFilePermissions(OterStatics.ANDROID_CERT_FILE, "644");
		
		// Deleting the temporary file
		logger.debug("Removing local temporary file: " + localFile);
		localFile.delete();
	}
	
	public static String findSystemDirectory() throws IOException, InterruptedException, CommandFailedException, AatException{
		// /dev/block/mmcblk0p12 /system ext3 ro,noatime,nodiratime,data=ordered 0 0
		logger.debug("Finding system directory");
		List<String> mountCmd = AdbWorkshop.runAdbCommand(new String[]{"shell", "mount"});
		for(String out : mountCmd){
			if(out.matches(".*dev.*block.*system.*")){
				logger.debug("Found mount line with system: " + out);
				String ret = out.split(" ")[0];
				logger.debug("Returning: " + ret);
				return(ret);
			}
		}
		throw new AatException("Could not determine the system directory.");
	}
	
	public static List<PackageBean> listPackages() throws IOException, InterruptedException, CommandFailedException, ParseException {
		logger.debug("Listing pacakges");
		List<String> packageStrings = AdbWorkshop.runAdbCommand(new String[]{"shell", "pm", "list", "packages", "-f"});
		List<PackageBean> ret = new LinkedList<PackageBean>();
		for(String s : packageStrings){
			ret.add(PackageBean.parse(s));
		}
		return(ret);
	}
	
	public static void uninstallPackage(String clazz) throws IOException, InterruptedException, CommandFailedException, UninstallException{
		logger.debug("Uninstalling package: " + clazz);
		List<String> out = AdbWorkshop.runAdbCommand(new String[]{"uninstall", clazz});
		for(String s : out){
			if(s.toLowerCase().matches(".*failure.*") == true){
				throw new UninstallException(s);
			}
		}
	}
}
