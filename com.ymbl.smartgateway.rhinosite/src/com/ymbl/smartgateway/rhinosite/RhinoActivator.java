/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.ymbl.smartgateway.rhinosite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ringojs.tools.launcher.Main;

import com.ymbl.smartgateway.rhinosite.log.SystemLogger;

public class RhinoActivator extends AbstractActivator implements Runnable{

	public final static String defaultHome = "/tmp/www";

	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info("Plug for Rhino Engine start ...");
        new Thread(this).start();
	}

	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub
		SystemLogger.info("Plug for Rhino Engine stop ...");
	}

	public void run() {
		// TODO Auto-generated method stub
		String ringoHome = defaultHome;
		try {
			ResourceBundle resource = ResourceBundle.getBundle("config");
			ringoHome = resource.getString("RingoHome");
		} catch (MissingResourceException e) {
			// TODO: handle exception
		} finally {
			SystemLogger.info("Ringo Home: " + ringoHome);
		}

		try {
			unZipFiles(ringoHome);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Start RingoJS Engine */
		System.setProperty("ringo.home", ringoHome);
		Main main = new Main();
        main.run(new String[]{ringoHome + "/main.js"});
        SystemLogger.info("Enter the JS Dir \"" + ringoHome + "\"");
	}

	public void unZipFiles(String descDir) throws IOException {

		InputStream is = RhinoActivator.class.getResourceAsStream("www.war");
		File tmpFile = File.createTempFile("www", "war");

		byte[] buf = new byte[1024];
		int len;

		OutputStream os = new FileOutputStream(tmpFile);
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
		is.close();
		os.close();

		File pathDescFile = new File(descDir);
		if(!pathDescFile.exists()) {
			pathDescFile.mkdirs();

			SystemLogger.info("Make new Source Dir \"" + descDir + "\"");

			ZipFile zip = new ZipFile(tmpFile);
			for(@SuppressWarnings("rawtypes")
			Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				String zipEntryName = entry.getName();
				InputStream in = zip.getInputStream(entry);
				String outPath = (descDir + "/" + zipEntryName).replaceAll("\\*", "/");
				File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
				if(!file.exists()) {
					file.mkdirs();
				}

				if(new File(outPath).isDirectory()) {
					continue;
				}

				OutputStream out = new FileOutputStream(outPath);
				while((len=in.read(buf))>0) {
					out.write(buf, 0 , len);
				}

				in.close();
				out.close();
			}
		}
		else {
			SystemLogger.info("\"" + descDir + "\" has already exist.");
		}
	}
}
