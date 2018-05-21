package com.ymbl.smartgateway.extension;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class Lua extends LoadLib {

	private static Lua myinstance = null;

	protected Lua(){
		addLoadLibsForNative(false);
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

	public static void ExcuteFromTelnet() {
		try {
			instanceWithNoload();
			myinstance.TelCommand("/tmp/transite-target/bin/lua "
					+ "/tmp/transite-target/etc/myplugin.lua &");
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
			loadFileFromJAR("lua", "/tmp/transite-target/bin", false, false);
			(new File("/tmp/transite-target/bin/lua")).setExecutable(true);
			loadFileFromJAR("myplugin.lua", "/tmp/transite-target/etc", false, false);
			loadFileFromJAR(Lua.class.getSimpleName()+".so", "/tmp/transite-target/lib", fornative, isreload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public native void exec(String cmd);
}
