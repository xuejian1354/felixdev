package com.ymbl.smartgateway.transite;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.ymbl.smartgateway.transite.log.SystemLogger;

public class PluginUtil {
	public static String doPost(String url, String params) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json");
		String charSet = "UTF-8";
		StringEntity entity = new StringEntity(params, charSet);
		httpPost.setEntity(entity);
		HttpResponse response = httpclient.execute(httpPost);
		StatusLine status = response.getStatusLine();
		int state = status.getStatusCode();
		if (state == HttpStatus.SC_OK) {
			HttpEntity responseEntity = response.getEntity();
			String jsonString = EntityUtils.toString(responseEntity);
			return jsonString;
		}
		else{
			SystemLogger.info("«Î«Û∑µªÿ:"+state+"("+url+")");
		}
		return null;
	}

	public static void  downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(3*1000);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        InputStream inputStream = conn.getInputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        byte[] getData = bos.toByteArray();

        File saveDir = new File(savePath);
        if(!saveDir.exists()){
            saveDir.mkdirs();
        }

        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos != null){
            fos.close();
        }

        if(inputStream != null){
            inputStream.close();
        }

        SystemLogger.info(url + " download success");
    }

	public static void unzip(String archive, String destinationDir) throws Exception {
	    final int BUFFER_SIZE = 1024;
	    SystemLogger.info("unzip " + archive + "...");
	    BufferedOutputStream dest = null;
	    FileInputStream fis = new FileInputStream(new File(archive));
	    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	    ZipEntry entry;
	    File destFile;
	    while ((entry = zis.getNextEntry()) != null) {
	        destFile = new File(destinationDir, entry.getName());
	        if (entry.isDirectory()) {
	            destFile.mkdirs();
	            continue;
	        } else {
	            int count;
	            byte data[] = new byte[BUFFER_SIZE];
	            destFile.getParentFile().mkdirs();
	            FileOutputStream fos = new FileOutputStream(destFile);
	            dest = new BufferedOutputStream(fos, BUFFER_SIZE);
	            while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
	                dest.write(data, 0, count);
	            }
	            dest.flush();
	            dest.close();
	            fos.close();

	            if (destFile.getParent().length() 
	            		- destFile.getParent().lastIndexOf("bin") == 3) {
		            destFile.setExecutable(true);	
				}
	        }
	    }
	    zis.close();
	    fis.close();

	    (new File(archive)).delete();
	}
}
