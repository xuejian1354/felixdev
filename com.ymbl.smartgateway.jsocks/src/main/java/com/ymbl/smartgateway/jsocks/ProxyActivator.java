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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.xbsafe.socks.ProxyClient;
import com.ymbl.smartgateway.jsocks.log.SystemLogger;

public class ProxyActivator extends AbstractActivator implements Runnable {

	private String serverAddr;
	int serverPort;

	protected void doStart() throws Exception {
		SystemLogger.info("Plug for Jsocks Engine start ...");
        new Thread(this).start();
	}

	protected void doStop() throws Exception {
		SystemLogger.info("Plug for Jsocks Engine stop ...");
	}

	public void run() {
		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			serverAddr = resource.getString("server");
			serverPort = Integer.parseInt(resource.getString("port"));

			SystemLogger.info("Connection: server = " + serverAddr + ", port = " + serverPort);

			ProxyClient.setLog(System.out);
			ProxyClient.ProxyServerAsClient(new Socket(serverAddr, serverPort));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingResourceException e) {
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
