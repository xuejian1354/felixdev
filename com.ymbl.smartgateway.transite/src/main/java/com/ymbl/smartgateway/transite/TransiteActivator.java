/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.ymbl.smartgateway.transite;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


//import com.chinamobile.smartgateway.commservices.DeviceInfoQueryService;
//import com.chinamobile.smartgateway.transferservices.TrafficForwardService;
import com.ymbl.smartgateway.transite.log.SystemLogger;

public class TransiteActivator extends AbstractActivator implements Runnable{

	public final static String defaultName = "trans-plugin";
	private String macdev = "eth0";

	//private DeviceInfoQueryService devInfoService;
	//private TrafficForwardService trafficService;

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		//devInfoService = (DeviceInfoQueryService) this.context.getService(
			//	this.context.getServiceReference(DeviceInfoQueryService.class.getName()));
		//trafficService = (TrafficForwardService) this.context.getService(
			//	this.context.getServiceReference(TrafficForwardService.class.getName()));

		SystemLogger.info("Plug start ...");
        new Thread(this).start();
	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info("Plug stop ...");
	}

	public void run() {
		// TODO Auto-generated method stub
		String plugName = defaultName;
		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			plugName = resource.getString("PluginName");
			macdev = resource.getString("MacDev");
		} catch (MissingResourceException e) {
			// TODO: handle exception
		} finally {
			SystemLogger.info("Plugin Name: " + plugName);
		}

		try {
			loadLib("transite");
			String macaddr = getMacAddr(macdev);
			SystemLogger.info("Get MAC from JNI: " + macaddr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//String mac = devInfoService.getDeviceMAC();
		//String osgiInfo = devInfoService.getOSGiInfo();

		//SystemLogger.info("Get Message MAC: " + mac + ", osgi: " + osgiInfo);
		//trafficService.addForwardRule("192.168.2.174", "8000-9000", "TCP", null, "192.168.2.1", 0);
		//SystemLogger.info("RuleInfo: " + trafficService.getForwardRuleInfo());
	}

	private synchronized static void loadLib(String libName) throws IOException {
		String systemType = System.getProperty("os.name");
		String libExtension = (systemType.toLowerCase().indexOf("win")!=-1) ? ".dll" : ".so";  
		String libFullName = libName + libExtension;
		String nativeTempDir = System.getProperty("java.io.tmpdir");
		InputStream in = null;
		BufferedInputStream reader = null;
		FileOutputStream writer = null;  

		File extractedLibFile = new File(nativeTempDir+File.separator+libFullName);
		if(!extractedLibFile.exists()) {
			try {
				in = TransiteActivator.class.getResourceAsStream("/" + libFullName);
				if(in==null)
					in =  TransiteActivator.class.getResourceAsStream(libFullName);
				TransiteActivator.class.getResource(libFullName);
				reader = new BufferedInputStream(in);
				writer = new FileOutputStream(extractedLibFile);
				
				byte[] buffer = new byte[1024];
				while (reader.read(buffer) > 0) {
					writer.write(buffer);
					buffer = new byte[1024];
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(in!=null)
					in.close();
				if(writer!=null)
					writer.close();
			}
        }
		System.load(extractedLibFile.toString());
	}

	public native String getMacAddr(String dev);
}
