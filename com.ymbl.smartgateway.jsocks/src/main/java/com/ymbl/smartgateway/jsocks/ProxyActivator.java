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
import com.ymbl.smartgateway.jsocks.log.SystemLogger;

public class ProxyActivator extends AbstractActivator implements Runnable {

	public final static String defaultHome = "/tmp/www";

	protected void doStart() throws Exception {
		SystemLogger.info("Plug for Jsocks Engine start ...");
        new Thread(this).start();
	}

	protected void doStop() throws Exception {
		SystemLogger.info("Plug for Jsocks Engine stop ...");
	}

	public void run() {
		String proxyHome = defaultHome;
		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			proxyHome = resource.getString("ProxyHome");
		} catch (MissingResourceException e) {
		} finally {
			SystemLogger.info("Proxy Home: " + proxyHome);
		}
	}
}
