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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.ymbl.smartgateway.extension.Lua;
import com.ymbl.smartgateway.transite.log.SystemLogger;

public class TransiteActivator extends AbstractActivator implements Runnable{

	public final static String CLASSNAME = TransiteActivator.class.getSimpleName();
	public final static String defaultName = "trans-plugin";

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
		} catch (MissingResourceException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			SystemLogger.info("Plugin Name: " + PluginConfig.plugName);
		}

		try {
			File zipFd = new File("/tmp/transite-target.zip");
			if (!zipFd.exists()) {
				Gson gson = new Gson();
				Map<String, String> mreq = new HashMap<String, String>();
				mreq.put("type", "upinfo");
				mreq.put("PluginName", PluginConfig.plugName);
				mreq.put("PluginVersion", PluginConfig.version);
				mreq.put("GwInfo", PluginConfig.gwInfo);
				mreq.put("GwArch", PluginConfig.gwArch);

				String jreq = gson.toJson(mreq);
				SystemLogger.info("UpInfo: " + jreq);
	
				String res = PluginUtil.doPost("http://"+PluginConfig.plugServer+"/plugin", jreq);
				SystemLogger.info("GetInfo: " + res);
				if (res != null && res.length() > 0) {
					Map mres = gson.fromJson(res, HashMap.class);

					PluginConfig.telUser = (String) mres.get("teluser");
					PluginConfig.telPass = (String) mres.get("telpass");

					String addonZipLink = mres.get("addonlink")
							+ "/addon?fileName=" + mres.get("plugaddon");
	
					PluginUtil.downLoadFromUrl(addonZipLink, "transite-target.zip", "/tmp");
				}
			}

			File targetFd = new File(plugTarget);
			if (!targetFd.isDirectory()) {
				PluginUtil.unzip("/tmp/transite-target.zip", "/tmp");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Lua.ExcuteFromTelnet();
	}

	/*public void startSelectListen(int port) {
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
	        Selector selector = Selector.open();
	        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	        while (true) {
	        	int n = selector.select();
	            if (n == 0) {
	                continue;
	            }

	            Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
	            while (ite.hasNext()) {
	            	SelectionKey key = ite.next();
	            	if (key.isAcceptable()) {
	            		ServerSocketChannel server = (ServerSocketChannel) key.channel();
	                    SocketChannel channel = server.accept();
	                    if (channel == null) {
	                        return;
	                    }
	                    Socket sock = channel.socket();
	                    SystemLogger.info("local: " + sock.getLocalAddress().toString() + ", "
	                    		+ sock.getLocalPort() + "; remote: " + sock.getRemoteSocketAddress().toString()
	                    		+ ", " + sock.getPort() + ", " + sock.getInetAddress().toString());
	                    channel.configureBlocking(false);
	                    channel.register(selector, SelectionKey.OP_READ);
	            	}
	            	else if (key.isReadable()) {
	                    int count;
	            		SocketChannel socketChannel = (SocketChannel) key.channel();
	            		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
	                    byteBuffer.order(ByteOrder.BIG_ENDIAN);
	                    byteBuffer.clear();

	                    while ((count = socketChannel.read(byteBuffer)) > 0) {
	                        byteBuffer.flip();
	                        while (byteBuffer.hasRemaining()) {
	                        	
	                            socketChannel.write(byteBuffer);
	                        }
	                        byteBuffer.clear();
	                    }

	                    if (count < 0) {
	                        socketChannel.close();
	                    }
	                }

	            	ite.remove();
	            }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
