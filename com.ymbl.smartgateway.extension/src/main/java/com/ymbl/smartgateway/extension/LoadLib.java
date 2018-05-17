package com.ymbl.smartgateway.extension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoadLib {

	private static LoadLib myinstance = null;

	protected LoadLib(){
		try {
			addLoadLibs(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static LoadLib instance(){
		if (myinstance == null) {
			myinstance = new LoadLib();
			System.out.println(LoadLib.class.getSimpleName() + " Instance ======>>>");
		}

		return myinstance;
	}

	public void addLoadLibs(boolean isreload) throws IOException {
		loadLib(LoadLib.class.getSimpleName()+".so", "/tmp/transite-target/lib", true, isreload);
	}

	protected synchronized static void loadLib(String libName, String targetDir, boolean fornative, boolean isreload) throws IOException {

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
}
