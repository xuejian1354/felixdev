package com.ymbl.smartgateway.extension;

public class Lua extends LoadLib {
	public native void exec(String cmd);
}
