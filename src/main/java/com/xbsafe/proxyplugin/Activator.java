package com.xbsafe.proxyplugin;

import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.xbsafe.socks.InetRange;
import com.xbsafe.socks.Proxy;
import com.xbsafe.socks.ProxyServer;
import com.xbsafe.socks.server.IdentAuthenticator;

class Socks {
	public static void socks() {
		int port = 8888;
		IdentAuthenticator auth = new IdentAuthenticator();
		OutputStream log = null;
		InetAddress localIP = null;
		inform("Loading properties");
		Properties pr = new Properties();
		if (!addAuth(auth, pr)) {
			usage();
			return;
		}
		inform("properties proxy info :" + auth + "\n");
		ProxyServer server = new ProxyServer(auth);
		server.setLog(log);
		server.start(port, 5, localIP);
	}

	/******************************/
	static public void usage() {
		System.out.println("read properties failed.\n");
	}

	// properties true path
	static boolean addAuth(IdentAuthenticator ident, Properties pr) {
		InetRange irange;
		// String range = (String) pr.get("range");
		String range = ".";
		irange = parseInetRange(range);
		String users = null;
		if (users == null) {
			ident.add(irange, null);
			return true;
		}
		Hashtable uhash = new Hashtable();
		StringTokenizer st = new StringTokenizer(users, ";");
		while (st.hasMoreTokens())
			uhash.put(st.nextToken(), "");
		ident.add(irange, uhash);
		return true;
	}

	static void serverInit(Properties props) {
		int val;
		val = readInt(props, "iddleTimeout");
		if (val >= 0) {
			ProxyServer.setIddleTimeout(val);
			inform("Setting iddle timeout to " + val + " ms.");
		}
		val = readInt(props, "acceptTimeout");
		if (val >= 0) {
			ProxyServer.setAcceptTimeout(val);
			inform("Setting accept timeout to " + val + " ms.");
		}
		val = readInt(props, "udpTimeout");
		if (val >= 0) {
			ProxyServer.setUDPTimeout(val);
			inform("Setting udp timeout to " + val + " ms.");
		}

		val = readInt(props, "datagramSize");
		if (val >= 0) {
			ProxyServer.setDatagramSize(val);
			inform("Setting datagram size to " + val + " bytes.");
		}

		proxyInit(props);

	}

	
	static void proxyInit(Properties props) {
		String proxy_list;
		Proxy proxy = null;
		StringTokenizer st;

		proxy_list = (String) props.get("proxy");
		if (proxy_list == null)
			return;

		st = new StringTokenizer(proxy_list, ";");
		while (st.hasMoreTokens()) {
			String proxy_entry = st.nextToken();

			Proxy p = Proxy.parseProxy(proxy_entry);

			if (p == null)
				exit("Can't parse proxy entry:" + proxy_entry);

			inform("Adding Proxy:" + p);

			if (proxy != null)
				p.setChainProxy(proxy);

			proxy = p;

		}
		if (proxy == null)
			return; // Empty list

		String direct_hosts = (String) props.get("directHosts");
		if (direct_hosts != null) {
			InetRange ir = parseInetRange(direct_hosts);
			inform("Setting direct hosts:" + ir);
			proxy.setDirect(ir);
		}

		ProxyServer.setProxy(proxy);
	}

	/**
	 * Inits range from the string of semicolon separated ranges.
	 */
	static InetRange parseInetRange(String source) {
		InetRange irange = new InetRange();

		StringTokenizer st = new StringTokenizer(source, ";");
		while (st.hasMoreTokens())
			irange.add(st.nextToken());

		return irange;
	}

	/**
	 * Integer representaion of the property named name, or -1 if one is not
	 * found.
	 */
	static int readInt(Properties props, String name) {
		int result = -1;
		String val = (String) props.get(name);
		if (val == null)
			return -1;
		StringTokenizer st = new StringTokenizer(val);
		if (!st.hasMoreElements())
			return -1;
		try {
			result = Integer.parseInt(st.nextToken());
		} catch (NumberFormatException nfe) {
			inform("Bad value for " + name + ":" + val);
		}
		return result;
	}

	// Display functions
	// /////////////////

	static void inform(String s) {
		System.out.println(s);
	}

	static void exit(String msg) {
		System.err.println("Error:" + msg);
		System.err.println("Aborting operation");
		System.exit(0);
	}
	/******************************/

}

class Mythread implements Runnable {

	public void run() {
		System.out.println("Æô¶¯Ïß³Ì£º" + Thread.currentThread().getName());
		Socks.socks();
	}
}


public class Activator implements BundleActivator {

	Thread threadMain = new Thread();

	public void start(BundleContext context) {
		Mythread mythread = new Mythread();
		threadMain = new Thread(mythread);
		threadMain.start();
	}

	public void stop(BundleContext context) throws Exception{
		try {
			System.out.println("ready to sleep");
			Thread.sleep(1000);
			System.out.println("ready to interrupt");
			threadMain.interrupt();
			System.out.println("ready stop");
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("stop compile");
	}

}
