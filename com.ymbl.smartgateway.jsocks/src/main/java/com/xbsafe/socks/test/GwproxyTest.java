package com.xbsafe.socks.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.xbsafe.socks.ProxyClient;

public class GwproxyTest {

	public static int CONN_NUMS = 4;
	public static String serverAddr = "192.168.2.198";
	public static int serverPort = 1081;

	public static void main(String[] argv) {
		int vgw_nums = CONN_NUMS;

		if (argv.length > 0) {
			vgw_nums = Integer.parseInt(argv[0]);
		}

		System.out.println("virtual gateway starting, nums=" + vgw_nums);

		try {
			ProxyClient.setLog(System.out);
			for (int i = 0; i < vgw_nums; i++) {
				ProxyClient.ProxyServerAsClient(new Socket(serverAddr, serverPort));	
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
