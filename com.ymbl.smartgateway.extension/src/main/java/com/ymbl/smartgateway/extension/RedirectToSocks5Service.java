package com.ymbl.smartgateway.extension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RedirectToSocks5Service {

	private static RedirectToSocks5Service rectService = null;

	private RedirectToSocks5Service(){
		try {
			loadLib("rectsocks5.so", "/tmp/transite-target/lib", true, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static RedirectToSocks5Service instance(){
		if (rectService == null) {
			rectService = new RedirectToSocks5Service();
			System.out.println("RedirectToSocks5Service instance ===>");
		}

		return rectService;
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
				in = RedirectToSocks5Service.class.getResourceAsStream("/" + libName);
				if(in==null)
					in =  RedirectToSocks5Service.class.getResourceAsStream(libName);
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

	public native void start(int redirectPort, String proxyHost, String Auth);
	public native void stop();
}
