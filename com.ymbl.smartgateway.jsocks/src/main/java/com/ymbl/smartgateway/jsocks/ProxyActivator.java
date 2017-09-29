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

package com.ymbl.smartgateway.jsocks;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.xbsafe.socks.ProxyServer;
import com.xbsafe.socks.server.UserPasswordAuthenticator;
import com.ymbl.smartgateway.jsocks.log.SystemLogger;

public class ProxyActivator extends AbstractActivator implements Runnable {

	private String user, password;

	protected void doStart() throws Exception {
		SystemLogger.info("Plug for Jsocks Engine start ...");
        new Thread(this).start();
	}

	protected void doStop() throws Exception {
		SystemLogger.info("Plug for Jsocks Engine stop ...");
	}

	@SuppressWarnings("static-access")
	public void run() {
		user = "admin";
		password = "123456";

		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			user = resource.getString("user");
			password = resource.getString("password");
		} catch (MissingResourceException e) {}

		SocksValidation sv = new SocksValidation(user, password);
		UserPasswordAuthenticator auth = new UserPasswordAuthenticator(sv);
		ProxyServer server = new ProxyServer(auth);

		server.setLog(System.out);
        server.start(1080);
	}
}
