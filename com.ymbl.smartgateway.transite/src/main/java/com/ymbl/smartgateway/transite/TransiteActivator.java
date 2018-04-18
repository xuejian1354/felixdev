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

			addLoadLibs();
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

	public void addLoadLibs() throws IOException {
		String[] xtables_lib_list = {
				"libip6t_DNAT.so", "libip6t_DNPT.so", "libip6t_HL.so", "libip6t_LOG.so", 
				"libip6t_MASQUERADE.so", "libip6t_NETMAP.so", "libip6t_REDIRECT.so", 
				"libip6t_REJECT.so", "libip6t_SNAT.so", "libip6t_SNPT.so", "libip6t_ah.so", 
				"libip6t_dst.so", "libip6t_eui64.so", "libip6t_frag.so", "libip6t_hbh.so", 
				"libip6t_hl.so", "libip6t_icmp6.so", "libip6t_ipv6header.so", "libip6t_mh.so", 
				"libip6t_rt.so", "libipt_CLUSTERIP.so", "libipt_DNAT.so", "libipt_ECN.so", 
				"libipt_LOG.so", "libipt_MASQUERADE.so", "libipt_MIRROR.so", "libipt_NETMAP.so",
				"libipt_REDIRECT.so", "libipt_REJECT.so", "libipt_SAME.so", "libipt_SNAT.so", 
				"libipt_TTL.so", "libipt_ULOG.so", "libipt_ah.so", "libipt_icmp.so", 
				"libipt_realm.so", "libipt_ttl.so", "libipt_unclean.so", "libxt_AUDIT.so", 
				"libxt_CHECKSUM.so", "libxt_CLASSIFY.so", "libxt_CONNMARK.so", 
				"libxt_CONNSECMARK.so", "libxt_CT.so", "libxt_DSCP.so", "libxt_HMARK.so",
				"libxt_IDLETIMER.so", "libxt_LED.so", "libxt_MARK.so", "libxt_NFLOG.so", 
				"libxt_NFQUEUE.so", "libxt_NOTRACK.so", "libxt_RATEEST.so", "libxt_SECMARK.so",
				"libxt_SET.so", "libxt_SYNPROXY.so", "libxt_TCPMSS.so", "libxt_TCPOPTSTRIP.so",
				"libxt_TEE.so", "libxt_TOS.so", "libxt_TPROXY.so", "libxt_TRACE.so",
				"libxt_addrtype.so", "libxt_bpf.so", "libxt_cluster.so", "libxt_comment.so", 
				"libxt_connbytes.so", "libxt_connlimit.so", "libxt_connmark.so", 
				"libxt_conntrack.so", "libxt_cpu.so", "libxt_dccp.so", "libxt_devgroup.so", 
				"libxt_dscp.so", "libxt_ecn.so", "libxt_esp.so", "libxt_hashlimit.so", 
				"libxt_helper.so", "libxt_iprange.so", "libxt_ipvs.so", "libxt_length.so",
				"libxt_limit.so", "libxt_mac.so", "libxt_mark.so", "libxt_multiport.so",
				"libxt_nfacct.so", "libxt_osf.so", "libxt_owner.so", "libxt_physdev.so", 
				"libxt_pkttype.so", "libxt_policy.so", "libxt_quota.so", "libxt_rateest.so",
				"libxt_recent.so", "libxt_rpfilter.so", "libxt_sctp.so", "libxt_set.so",
				"libxt_socket.so", "libxt_standard.so", "libxt_state.so", "libxt_statistic.so",
				"libxt_string.so", "libxt_tcp.so", "libxt_tcpmss.so", "libxt_time.so", 
				"libxt_tos.so", "libxt_u32.so", "libxt_udp.so"
		};

		loadLib("libip4tc.so.0", "/tmp/transite-target/lib", false, false);
		loadLib("libip6tc.so.0", "/tmp/transite-target/lib", false, false);
		loadLib("libxtables.so.10", "/tmp/transite-target/lib", false, false);
		for (String xtlib : xtables_lib_list) {
			loadLib(xtlib, "/tmp/transite-target/lib/xtables", false, false);
		}

		loadLib("transite.so", "/tmp/transite-target/lib", true, loadreslib);
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

	private synchronized static void loadLib(String libName, String targetDir, boolean fornative, boolean isreload) throws IOException {

		InputStream in = null;
		BufferedInputStream reader = null;
		FileOutputStream writer = null;  

		File ftd = new File(targetDir);
		if (ftd.isFile()) {
			ftd.delete();
		}

		if (!ftd.exists()) {
			ftd.mkdirs();
		}

		File extractedLibFile = new File(targetDir+"/"+libName);
		if (isreload && extractedLibFile.exists()) {
			extractedLibFile.delete();
		}

		if(!extractedLibFile.exists()) {
			try {
				in = TransiteActivator.class.getResourceAsStream("/" + libName);
				if(in==null)
					in =  TransiteActivator.class.getResourceAsStream(libName);
				TransiteActivator.class.getResource(libName);
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

		if (fornative) {
			System.load(extractedLibFile.toString());			
		}
	}

	public native byte[] getMacAddr(String dev);
	public native int iptables(String rule);
}
