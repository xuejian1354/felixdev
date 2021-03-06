package com.ymbl.smartgateway.extension;

import java.io.IOException;

public class RedirectToSocks5Service extends LoadLib {

	private static RedirectToSocks5Service myinstance = null;

	protected RedirectToSocks5Service(){
		//addLoadLibsForNative(false);
	}

	public static RedirectToSocks5Service instance(){
		if (myinstance == null) {
			myinstance = new RedirectToSocks5Service();
			System.out.println(RedirectToSocks5Service.class.getSimpleName() + " Instance ======>>>");
		}

		return myinstance;
	}

	@Override
	public void addLoadLibs(Boolean fornative, boolean isreload) {
		// TODO Auto-generated method stub
		try {
			loadFileFromJAR(RedirectToSocks5Service.class.getSimpleName()+".so", "/tmp/transite-target/lib", fornative, isreload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startEx(int redirectPort, String proxyHost, String Auth) {
		try {
			start(redirectPort, proxyHost, Auth);
		} catch (UnsatisfiedLinkError e) {
			// TODO: handle exception
			addLoadLibsForNative(false);
			start(redirectPort, proxyHost, Auth);
		}
	}

	public void stopEx() {
		try {
			stop();
		} catch (UnsatisfiedLinkError e) {
			// TODO: handle exception
			addLoadLibsForNative(false);
			stop();
		}
	}

	private native void start(int redirectPort, String proxyHost, String Auth);
	private native void stop();
}
