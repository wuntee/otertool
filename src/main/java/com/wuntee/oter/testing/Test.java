package com.wuntee.oter.testing;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		AndroidDebugBridge.init(true);
		AndroidDebugBridge adb = AndroidDebugBridge.createBridge("/Applications/android-sdk-macosx/platform-tools/adb", true);
		
		while(!adb.hasInitialDeviceList()){
			System.out.println("Waiting...");
			Thread.sleep(1000);
		}
		for(IDevice dev : adb.getDevices()){
			System.out.println(dev);
			for(Client client : dev.getClients()){
				System.out.println(" -" + client);
			}
		}
		
		IDevice dev = adb.getDevices()[0];
		Client cli = dev.getClients()[0];
		
		AndroidDebugBridge.disconnectBridge();
	}

}
