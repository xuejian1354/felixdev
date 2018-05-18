package com.ymbl.smartgateway.extension;

import java.io.IOException;

public class XL2tpd extends LoadLib {

	@Override
	public void addLoadLibs(boolean isreload) throws IOException {
		// TODO Auto-generated method stub
		loadFileFromJAR("xl2tpd", "/tmp/transite-target/bin", false, false);
		loadFileFromJAR("pppd", "/tmp/transite-target/bin", false, false);
		loadFileFromJAR("openl2tp.so", "/tmp/transite-target/lib/pppd/2.4.7", false, false);
		loadFileFromJAR("pppol2tp.so", "/tmp/transite-target/lib/pppd/2.4.7", false, false);
		super.addLoadLibs(isreload);
	}

	public native void start();
	public native void route();
}
