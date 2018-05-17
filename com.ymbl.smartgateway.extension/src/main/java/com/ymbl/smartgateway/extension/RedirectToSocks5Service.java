package com.ymbl.smartgateway.extension;

public class RedirectToSocks5Service extends LoadLib {
	public native void start(int redirectPort, String proxyHost, String Auth);
	public native void stop();
}
