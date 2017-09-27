package com.xbsafe.socks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Http {
	public static String sendGet(String url, String parameter) {
		String result = "";
		BufferedReader input = null;
		try {
			URLConnection connection = (new URL(parameter == null ? url : url
					+ "?" + parameter)).openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			// connection.setRequestProperty("user-agent",
			// "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			/*
			 * Map<String, List<String>> map = connection.getHeaderFields(); for
			 * (String key : map.keySet()) { System.out.println(key + " : " +
			 * map.get(key)); }
			 */
			input = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line = "";
			while ((line = input.readLine()) != null) {
				result += line;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception exceptionClose) {
					exceptionClose.printStackTrace();
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		// System.out.println(Http.sendGet("http://localhost:8888", ""));
		System.out.println(Http.sendGet("http://27.129.25.182:8888", null));
	}
}