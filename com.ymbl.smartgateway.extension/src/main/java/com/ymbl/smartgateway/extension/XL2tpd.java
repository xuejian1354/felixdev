package com.ymbl.smartgateway.extension;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class XL2tpd extends LoadLib {

	private static XL2tpd myinstance = null;

	protected XL2tpd(){
		addLoadLibsForNative(false);
	}

	protected XL2tpd(boolean fornative) {
		if (fornative) {
			addLoadLibsForNative(false);
		}
		else {
			addLoadLibsNoNative(false);
		}
	}

	public static XL2tpd instance(){
		if (myinstance == null) {
			myinstance = new XL2tpd();
			System.out.println(XL2tpd.class.getSimpleName() + " Instance ======>>>");
		}

		return myinstance;
	}
	
	public static XL2tpd instanceWithNoload() {
		if (myinstance == null) {
			myinstance = new XL2tpd(false);
			System.out.println(XL2tpd.class.getSimpleName() + " Instance ======>>>");
		}

		return myinstance;
	}

	public static void ExcuteFromTelnet() {
		try {
			instanceWithNoload();
			myinstance.TelCommand("/tmp/transite-target/bin/xl2tpd "
					+ "-c /tmp/transite-target/etc/xl2tpd.conf -D &");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addLoadLibs(Boolean fornative, boolean isreload) {
		// TODO Auto-generated method stub
		try {
			loadFileFromJAR("xl2tpd", "/tmp/transite-target/bin", false, false);
			loadFileFromJAR("pppd", "/tmp/transite-target/bin", false, false);
			loadFileFromJAR("xl2tpd.conf", "/tmp/transite-target/etc", false, false);
			loadFileFromJAR("options.l2tpd.client", "/tmp/transite-target/etc", false, false);
			loadFileFromJAR("options", "/tmp/transite-target/etc/ppp", false, false);
			loadFileFromJAR("openl2tp.so", "/tmp/transite-target/lib/pppd/2.4.7", false, false);
			loadFileFromJAR("pppol2tp.so", "/tmp/transite-target/lib/pppd/2.4.7", false, false);

			File xfd = new File("/tmp/transite-target/bin/xl2tpd");
			xfd.setExecutable(true);
			File pfd = new File("/tmp/transite-target/bin/pppd");
			pfd.setExecutable(true);
			File fd = new File("/tmp/transite-target/var/run/");
			fd.mkdirs();
			loadFileFromJAR(XL2tpd.class.getSimpleName()+".so", "/tmp/transite-target/lib", fornative, isreload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public native void start();
	public native void route();
}
