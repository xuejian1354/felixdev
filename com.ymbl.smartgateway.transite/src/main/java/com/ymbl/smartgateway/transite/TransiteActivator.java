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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ymbl.smartgateway.transite.log.SystemLogger;

public class TransiteActivator extends AbstractActivator implements Runnable{

	public final static String CLASSNAME = TransiteActivator.class.getName();
	public final static String defaultName = "trans-plugin";
	private String macdev = "eth0";
	private static boolean loadreslib = false;
	private static int redirectPort = 0;

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

			String macstrcfg = resource.getString("MacDev");
			if (macstrcfg != null && macstrcfg.length() > 0) {
				macdev = macstrcfg;
			}

			String loadlibstrcfg = resource.getString("LoadLibRes");
			if (loadlibstrcfg != null && loadlibstrcfg.equals("true")) {
				loadreslib = true;
			}

			redirectPort = Integer.valueOf(resource.getString("RedirectPort"));
			if (redirectPort <= 0) {
				SystemLogger.info("get RedirectPort error");
				return;
			}

			loadLib("transite");
		} catch (MissingResourceException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			SystemLogger.info("Plugin Name: " + plugName);
		}

		getMacFromJNITest();
		addRedirectHostRule();

		startSelectListen(redirectPort);
	}

	public void getMacFromJNITest() {
		byte[] macaddr = getMacAddr(macdev);
		if (macaddr != null) {
			String macstr = "";
			for (byte b : macaddr) {
				macstr += String.format("%02X:", new Integer(b&0xFF));
			}
			macstr = macstr.substring(0, macstr.length()-1);
			SystemLogger.info("Get MAC from JNI: " + macstr);
		}
		else {
			SystemLogger.info(macdev + " doesn't get mac address");
		}
	}

	public void startSelectListen(int port) {
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
	}

	public void addRedirectHostRule() {
		iptables("iptables -t nat -A PREROUTING -p tcp -d 31.13.86.8 --dport " + 80 + " -j REDIRECT --to-port " + redirectPort);
		iptables("iptables -t nat -A PREROUTING -p tcp -d 106.39.167.118 --dport " + 80 + " -j REDIRECT --to-port " + redirectPort);
		iptables("iptables -t nat -nvL PREROUTING");
		iptables("iptables -t nat -D PREROUTING 1");
		iptables("iptables -t nat -D PREROUTING 1");
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
		if(!extractedLibFile.exists() || loadreslib == true) {
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

	public native byte[] getMacAddr(String dev);
	public native int iptables(String rule);
}
