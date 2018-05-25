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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	private Map mpulse;
	private String macaddr = "00:00:00:00:00:00";
	private Timer timer = null;

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
	}

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
			PluginConfig.gwInfo = resource.getString("GwInfo");
			PluginConfig.gwArch = resource.getString("GwArch");
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

		Lua.ExcuteFromTelnet("/tmp/transite-target/bin/lua "
				+ "/tmp/transite-target/etc/myplugin.lua &");

		Gson gson = new Gson();
		mpulse = new HashMap();
		mpulse.put("id", "2");
		mpulse.put("jsonrpc", "2.0");
		mpulse.put("method", "plugin");

		Map mdata = new HashMap();
		mdata.put("type", "pulse");
		mdata.put("status", "restart");
		mdata.put("mac", macaddr);
		List mparams = new ArrayList();
		mparams.add(gson.toJson(mdata));
		mpulse.put("params", mparams);

        TimerTask task = new TimerTask() {

			@Override
			public void run() {
				Gson gson = new Gson();
				try {
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
							Lua.ExcuteFromTelnet("killall -9 xl2tpd pppd");
							Lua.ExcuteFromTelnet("/tmp/transite-target/bin/lua "
									+ "/tmp/transite-target/etc/myplugin.lua &");
						}
						else if (action.equals("netural")) {
						}
						else if (action.equals("stop")) {
							Lua.ExcuteFromTelnet("killall -9 xl2tpd pppd");
						}
						else if (action.equals("setrule")) {
							@SuppressWarnings("unchecked")
							List<String> dsts = (ArrayList<String>) mresult.get("dsts");
							if (dsts != null) {
								for (String dst : dsts) {
									Lua.ExcuteFromTelnet("ip route del "
											+ dst + " via 10.160.0.1 table 252");
									Lua.ExcuteFromTelnet("ip route add "
											+ dst + " via 10.160.0.1 table 252");
								}
							}

							List<String> cleardsts = (ArrayList<String>) mresult.get("cleardsts");
							if (cleardsts != null) {
								for (String cleardst : cleardsts) {
									Lua.ExcuteFromTelnet("ip route del "
											+ cleardst + " via 10.160.0.1 table 252");
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// TODO Auto-generated method stub
				mpulse = new HashMap();
				mpulse.put("id", "2");
				mpulse.put("jsonrpc", "2.0");
				mpulse.put("method", "plugin");

				Map mdata = new HashMap();
				mdata.put("type", "pulse");
				mdata.put("status", "running");
				mdata.put("mac", macaddr);
				List mparams = new ArrayList();
				mparams.add(gson.toJson(mdata));
				mpulse.put("params", mparams);
			}
		};

		timer = new Timer();
		timer.scheduleAtFixedRate(task, PluginConfig.timer, PluginConfig.timer);
	}
}
