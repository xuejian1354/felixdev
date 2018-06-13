package com.ymbl.smartgateway.extension;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class Lua extends LoadLib {

	private static Lua myinstance = null;

	protected Lua(){
		//addLoadLibsForNative(false);
	}

	public static Lua instance(){
		if (myinstance == null) {
			myinstance = new Lua();
			System.out.println(Lua.class.getSimpleName() + " Instance ======>>>");
		}

		return myinstance;
	}

	protected Lua(boolean fornative) {
		if (fornative) {
			addLoadLibsForNative(false);
		}
		else {
			addLoadLibsNoNative(false);
		}
	}

	public static Lua instanceWithNoload() {
		if (myinstance == null) {
			myinstance = new Lua(false);
			System.out.println(Lua.class.getSimpleName() + " Instance ======>>>");
		}

		return myinstance;
	}

	public static String ExcuteFromTelnet(String cmd) {
		try {
			instanceWithNoload();
			return myinstance.TelCommand(cmd);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void addLoadLibs(Boolean fornative, boolean isreload) {
		// TODO Auto-generated method stub
		try {
			loadFileFromJAR("lua", "/tmp/transite-target/bin", false, false);
			File lfd = new File("/tmp/transite-target/bin/lua");
			if (lfd.exists()) {
				lfd.setExecutable(true);
			}
			loadFileFromJAR("myplugin.lua", "/tmp/transite-target/etc", false, false);
			loadFileFromJAR(Lua.class.getSimpleName()+".so", "/tmp/transite-target/lib", fornative, isreload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] getMacAddrEx(String dev) {
		try {
			return getMacAddr(dev);
		} catch (UnsatisfiedLinkError e) {
			// TODO: handle exception
			addLoadLibsForNative(false);
			return getMacAddr(dev);
		}
	}

	public void execEx(String cmd) {
		try {
			exec(cmd);
		} catch (UnsatisfiedLinkError e) {
			// TODO: handle exception
			addLoadLibsForNative(false);
			exec(cmd);
		}
	}

	private native byte[] getMacAddr(String dev);
	private native void exec(String cmd);
}
