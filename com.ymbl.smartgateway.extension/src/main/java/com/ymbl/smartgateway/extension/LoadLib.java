package com.ymbl.smartgateway.extension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;

import com.ymbl.smartgateway.transite.PluginConfig;
import com.ymbl.smartgateway.transite.log.SystemLogger;

abstract public class LoadLib {
	public void addLoadLibsForNative(boolean isreload) {
		addLoadLibs(true, isreload);
	}

	public void addLoadLibsNoNative(boolean isreload) {
		addLoadLibs(false, isreload);
	}

	abstract public void addLoadLibs(Boolean fornative, boolean isreload);

	protected synchronized static void loadFileFromJAR(String libName, String targetDir, boolean fornative, boolean isreload) throws IOException {
		int len;
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
				in = LoadLib.class.getResourceAsStream("/" + libName);
				if(in==null)
					in =  LoadLib.class.getResourceAsStream(libName);
				reader = new BufferedInputStream(in);
				writer = new FileOutputStream(extractedLibFile);

				byte[] buffer = new byte[1024];
				while ((len=reader.read(buffer)) > 0) {
					writer.write(buffer, 0, len);
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

	public String TelCommand(String cmd) throws SocketException, IOException {
		TelnetClient tc = new TelnetClient("vt200");
		tc.setDefaultTimeout(5000);
		tc.connect("127.0.0.1", PluginConfig.telPort);
		InputStream ins = tc.getInputStream();
		OutputStream outs = tc.getOutputStream();
		SystemLogger.info(readUtil(":", ins));
		writeUtil(PluginConfig.telUser, outs);
		SystemLogger.info(readUtil(":", ins));
		writeUtil(PluginConfig.telPass, outs);
		writeUtil("admin", outs);
		String pass = readUtil(":", ins);
		if (pass.length() < 1) {
			return null;
		}
		writeUtil(cmd, outs);
		String ret = readUtil("#", ins);
		SystemLogger.info(ret);
		tc.disconnect();
		return ret;
	}

	public void writeUtil(String cmd, OutputStream os) throws IOException {
		cmd = cmd +  "\n";
		os.write(cmd.getBytes());
		os.flush();
	}

	public String readUtil(String endFlag, InputStream in) throws IOException {
		InputStreamReader isr = new InputStreamReader(in);
		
		char[] charBytes = new char[1024];
		int n = 0;
		boolean flag = false;
		String str = "";
		while ((n = isr.read(charBytes)) != -1) {
			for (int i = 0; i < n; i++) {
				char c = (char)charBytes[i];
				str += c;
				if (str.endsWith(endFlag)) {
					flag = true;
					break;
				}
			}

			if (flag) {
				break;
			}
		}

		if (flag == false) {
			str = "";
		}

		return str;
	}
}
