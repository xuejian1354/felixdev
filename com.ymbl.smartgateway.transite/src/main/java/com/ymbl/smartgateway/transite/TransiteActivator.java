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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ymbl.smartgateway.extension.Lua;
import com.ymbl.smartgateway.extension.XL2tpd;
import com.ymbl.smartgateway.transite.log.SystemLogger;

public class TransiteActivator extends AbstractActivator implements Runnable{

	public final static String CLASSNAME = TransiteActivator.class.getName();
	public final static String defaultName = "trans-plugin";
	/*private int redirectPort = 0;
	private String proxyHost = "0.0.0.0:1080";
	private String socksAuth = null;*/

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info(CLASSNAME.substring(CLASSNAME.lastIndexOf('.')) + " start ...");
        new Thread(this).start();
	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info(CLASSNAME.substring(CLASSNAME.lastIndexOf('.')) + " stop ...");
	}

	public void run() {
		// TODO Auto-generated method stub
		String plugName = defaultName;
		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			plugName = resource.getString("PluginName");
			if (plugName == null || plugName.equals("")) {
				plugName = defaultName;
			}

			/*redirectPort = Integer.valueOf(resource.getString("RedirectPort"));
			if (redirectPort <= 0) {
				SystemLogger.info("get RedirectPort error");
				return;
			}

			String proxyHostStr = resource.getString("ProxyHost");
			if (proxyHostStr != null && proxyHostStr.length() > 0) {
				proxyHost = proxyHostStr;
			}

			String authstr = resource.getString("ProxyAuth");
			if(authstr != null && authstr.length() > 0) {
				socksAuth = authstr;
			}*/

			/*IpTables iptables = IpTables.instance();
			String RedirectTables = resource.getString("RedirectTables");
			StringTokenizer retabtoken = new StringTokenizer(RedirectTables, ";");
			while (retabtoken.hasMoreElements()) {
				 String[] rehost = retabtoken.nextToken().split(":");
				 if (rehost.length > 2) {
					 iptables.rule("iptables -t nat -I PREROUTING -p tcp -d " 
							 + rehost[0] + " --dport " + rehost[1] 
							 + " -j REDIRECT --to " + redirectPort);
				}
			}
			iptables.rule("iptables -t nat -nvL PREROUTING");*/

			/*RedirectToSocks5Service rectservice = (RedirectToSocks5Service) RedirectToSocks5Service.instance();
			rectservice.start(redirectPort, proxyHost, socksAuth);
			rectservice.stop();*/

			//XL2tpd.ExcuteFromTelnet();
			//startSelectListen(redirectPort);
		} catch (MissingResourceException e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			SystemLogger.info("Plugin Name: " + plugName);
		}

		XL2tpd.instanceWithNoload();
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
