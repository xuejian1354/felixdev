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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.ymbl.smartgateway.extension.Lua;
import com.ymbl.smartgateway.transite.log.SystemLogger;

public class TransiteActivator extends AbstractActivator implements Runnable{

	public final static String CLASSNAME = TransiteActivator.class.getSimpleName();
	public final static String defaultName = "trans-plugin";

	private String macaddr = "00:00:00:00:00:00";
	private Timer timer = null;
	private boolean isNeedRestart = true;
	private boolean isStop = false;
	private String status = "restart";

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info(CLASSNAME + " start ...");
        new Thread(this).start();
	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info(CLASSNAME + " stop ...");
		if (timer != null) {
			timer.cancel();
		}
		Lua.ExcuteFromTelnet("killall -9 xl2tpd pppd lua");
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
	public void run() {
		// TODO Auto-generated method stub
		PluginConfig.plugName = defaultName;
		String plugTarget = "/tmp/transite-target";
		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			PluginConfig.plugName = resource.getString("PluginName");
			if (PluginConfig.plugName == null || PluginConfig.plugName.equals("")) {
				PluginConfig.plugName = defaultName;
			}
			PluginConfig.version = resource.getString("PluginVersion");
			PluginConfig.plugServer = resource.getString("PluginServer");
			PluginConfig.vpnServer = resource.getString("VPNServer");
			PluginConfig.gwInfo = resource.getString("GwInfo");
			PluginConfig.gwArch = resource.getString("GwArch");
			PluginConfig.gwclib = resource.getString("GwCLib");
			plugTarget = resource.getString("PluginTarget");
			PluginConfig.timer = Integer.parseInt(resource.getString("Timer"));
			PluginConfig.macdev = resource.getString("MacDev");
		} catch (MissingResourceException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			SystemLogger.info("Plugin Name: " + PluginConfig.plugName);
		}

		try {
			Gson gson = new Gson();
			Map mreq = new HashMap();
			mreq.put("id", "1");
			mreq.put("jsonrpc", "2.0");
			mreq.put("method", "plugin");

			Map mdata = new HashMap();
			mdata.put("type", "upinfo");
			mdata.put("pluginname", PluginConfig.plugName);
			mdata.put("pluginversion", PluginConfig.version);
			mdata.put("gwinfo", PluginConfig.gwInfo);
			mdata.put("gwarch", PluginConfig.gwArch);
			mdata.put("gwclib", PluginConfig.gwclib);
			List mparams = new ArrayList();
			mparams.add(gson.toJson(mdata));
			mreq.put("params", mparams);

			String jreq = gson.toJson(mreq);
			SystemLogger.info("UpInfo: " + jreq);

			String res = PluginUtil.doPost("http://"+PluginConfig.plugServer+"/jsonRpc", jreq);
			SystemLogger.info("GetInfo: " + res);
			if (res != null && res.length() > 0) {
				Map mres = gson.fromJson(res, HashMap.class);
				Map mresult = (LinkedTreeMap)mres.get("result");

				PluginConfig.telUser = (String) mresult.get("teluser");
				PluginConfig.telPass = (String) mresult.get("telpass");
				try {
					PluginConfig.telPort = Integer.parseInt((String) mresult.get("telport"));
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				if (PluginConfig.telPort <= 0) {
					PluginConfig.telPort = 23;
				}

				File targetFd = new File(plugTarget);
				if (!targetFd.isDirectory()) {
					String addonZipLink = mresult.get("addonlink")
							+ "/addon?fileName=" + mresult.get("plugaddon");

					PluginUtil.downLoadFromUrl(addonZipLink, "transite-target.zip", "/tmp");
					PluginUtil.unzip("/tmp/transite-target.zip", "/tmp");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			File macFd = new File("/sys/class/net/"+ PluginConfig.macdev +"/address");
			FileReader reader = new FileReader(macFd);
			BufferedReader br = new BufferedReader(reader);
			macaddr = br.readLine();
			SystemLogger.info("getMacAddr: " + macaddr);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Lua.ExcuteFromTelnet("echo \"1\" > /proc/sys/net/ipv4/ip_forward; "
				+ "iptables -t nat -D POSTROUTING -o ppp0 -j MASQUERADE; "
				+ "iptables -t nat -A POSTROUTING -o ppp0 -j MASQUERADE;");

		timer = new Timer();
        TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Gson gson = new Gson();
				try {
					File procFd = new File("/proc");
					String[] psFds = procFd.list();
					boolean isPPPDRunning = false;
					for (String psFd: psFds) {
						File cmdFd = new File("/proc/"+psFd+"/cmdline");
						if (cmdFd.isFile()) {
							BufferedReader br = new BufferedReader(
									new InputStreamReader(new FileInputStream(cmdFd)));
							String data = null;
							while((data=br.readLine()) != null)
							{
								if (data.indexOf("pppd") >= 0) {
									isPPPDRunning = true;
									break;
								}
							}

							if (isPPPDRunning) {
								break;
							}
						}
					}

					if (isPPPDRunning) {
						status = "running";
					}
					else if (!status.equals("restart")) {
						status = "stoping";
						isNeedRestart = true;
					}

					// TODO Auto-generated method stub
					Map  mpulse = new HashMap();
					mpulse.put("id", "2");
					mpulse.put("jsonrpc", "2.0");
					mpulse.put("method", "plugin");

					Map mdata = new HashMap();
					mdata.put("type", "pulse");
					mdata.put("status", status);
					mdata.put("mac", macaddr);
					List mparams = new ArrayList();
					mparams.add(gson.toJson(mdata));
					mpulse.put("params", mparams);

					String jreq = gson.toJson(mpulse);
					SystemLogger.info("UpInfo: " + jreq);
					String res = PluginUtil.doPost(
							"http://"+PluginConfig.plugServer+"/jsonRpc", 
							jreq);
					SystemLogger.info("GetInfo: " + res);
					if (res != null && res.length() > 0) {
						Map mres = gson.fromJson(res, HashMap.class);
						Map mresult = (LinkedTreeMap)mres.get("result");
						String action = (String)mresult.get("action");
						if (action.equals("start")) {
							isNeedRestart = true;
							isStop = false;
						}
						int interval = Integer.parseInt((String)mresult.get("interval"));
						if (interval > 0) {
							Field field = TimerTask.class.getDeclaredField("period");
							field.setAccessible(true);
							field.set(this, PluginConfig.timer+interval);
						}

						if (isNeedRestart && !isStop) {
							Lua.ExcuteFromTelnet("killall -9 xl2tpd pppd lua");
							Lua.ExcuteFromTelnet("/tmp/transite-target/bin/lua "
									+ "/tmp/transite-target/etc/myplugin.lua "
									+ PluginConfig.vpnServer + " &");
							isNeedRestart = false;
						}
						else if (action.equals("netural")) {
						}
						else if (action.equals("stop")) {
							Lua.ExcuteFromTelnet("killall -9 xl2tpd pppd lua sh");
							isStop = true;
						}
						else if (action.equals("setrule")) {
							String cmdline = "";
							List<String> dsts = (ArrayList<String>) mresult.get("dsts");
							if (dsts != null && !dsts.isEmpty()) {
								for (String dst : dsts) {
									cmdline += "ip route del "
											+ dst + " via 10.160.0.1 table 252; ip route add "
											+ dst + " via 10.160.0.1 table 252; ";
								}
							}

							List<String> cleardsts = (ArrayList<String>) mresult.get("cleardsts");
							if (cleardsts != null && !cleardsts.isEmpty()) {
								for (String cleardst : cleardsts) {
									cmdline += "ip route del "
											+ cleardst + " via 10.160.0.1 table 252; ";
								}
							}

							if (cmdline.length() > 0) {
								Lua.ExcuteFromTelnet(cmdline);
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		timer.scheduleAtFixedRate(task, 1000, PluginConfig.timer);
	}
}
