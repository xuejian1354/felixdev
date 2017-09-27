package com.xbsafe.socks.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import com.xbsafe.socks.ProxyMessage;
import com.xbsafe.socks.UDPEncapsulation;

/**
 * An implementation of ServerAuthenticator, which does <b>not</b> do any
 * authentication.
 * <P>
 * <FONT size="+3" color ="FF0000"> Warning!!</font><br>
 * Should not be used on machines which are not behind the firewall.
 * <p>
 * It is only provided to make implementing other authentication schemes easier.
 * <br>
 * For Example: <tt><pre>
   class MyAuth extends socks.server.ServerAuthenticator{
    ...
    public ServerAuthenticator startSession(java.net.Socket s){
      if(!checkHost(s.getInetAddress()) return null;
      return super.startSession(s);
    }

    boolean checkHost(java.net.Inetaddress addr){
      boolean allow;
      //Do it somehow
      return allow;
    }
   }
</pre></tt>
 */

public class ServerAuthenticatorNone implements ServerAuthenticator {
	// 这是想干啥？
	static final byte[] socks5response = { 5, 0 };

	InputStream in;
	OutputStream out;

	/**
	 * Creates new instance of the ServerAuthenticatorNone.
	 */
	// 定义一个自身函数，干啥呢？
	public ServerAuthenticatorNone() {
		// this.in ???嘛意思？
		this.in = null;
		this.out = null;
	}

	/**
	 * Constructs new ServerAuthenticatorNone object suitable for returning from
	 * the startSession function.
	 * 
	 * @param in
	 *            Input stream to return from getInputStream method.
	 * @param out
	 *            Output stream to return from getOutputStream method.
	 */
	// 又定义一个带参函数 可接收输入输出流，为命令 和结果做准备？
	public ServerAuthenticatorNone(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	/**
	 * Grants access to everyone.Removes authentication related bytes from the
	 * stream, when a SOCKS5 connection is being made, selects an authentication
	 * NONE.
	 */
	// 重写父类 startSession方法
	public ServerAuthenticator startSession(Socket s) throws IOException {

		// 回退流 将不需要的数据 回退回去进行处理
		PushbackInputStream in = new PushbackInputStream(s.getInputStream());
		//
		OutputStream out = s.getOutputStream();
		// 读取 socks 是否版本是 5
		int version = in.read();
		if (version == 5) {
			// 设置验证，输入输出是否如愿
			if (!selectSocks5Authentication(in, out, 0))
				return null;
		} else if (version == 4) {
			// Else it is the request message allready, version 4
			in.unread(version);
		} else
			return null;
		// 当判断为真正的socks5，即将此输入输出定位标准
		return new ServerAuthenticatorNone(in, out);
	}

	/**
	 * Get input stream.
	 * 
	 * @return Input stream speciefied in the constructor.
	 */
	public InputStream getInputStream() {
		return in;
	}

	/**
	 * Get output stream.
	 * 
	 * @return Output stream speciefied in the constructor.
	 */
	public OutputStream getOutputStream() {
		return out;
	}

	/**
	 * Allways returns null.
	 * 
	 * @return null
	 */
	// 定义获取udp消息函数
	public UDPEncapsulation getUdpEncapsulation() {
		return null;
	}

	/**
	 * Allways returns true.
	 */
	// 去检查是否通过 请求回复
	public boolean checkRequest(ProxyMessage msg) {
		return true;
	}

	/**
	 * Allways returns true.
	 */
	// 参数为 接收数据包 输出流的bool值？
	public boolean checkRequest(java.net.DatagramPacket dp, boolean out) {
		return true;
	}

	/**
	 * Does nothing.
	 */
	public void endSession() {
	}

	/**
	 * Convinience routine for selecting SOCKSv5 authentication.
	 * <p>
	 * This method reads in authentication methods that client supports, checks
	 * wether it supports given method. If it does, the notification method is
	 * written back to client, that this method have been chosen for
	 * authentication. If given method was not found, authentication failure
	 * message is send to client ([5,FF]).
	 * 
	 * @param in
	 *            Input stream, version byte should be removed from the stream
	 *            before calling this method.
	 * @param out
	 *            Output stream.
	 * @param methodId
	 *            Method which should be selected.
	 * @return true if methodId was found, false otherwise.
	 */
	// 验证是否是 socks5 的函数
	static public boolean selectSocks5Authentication(InputStream in,
			OutputStream out, int methodId) throws IOException {
		int num_methods = in.read();
		// 得到的版本号 进行判断
		if (num_methods <= 0)
			return false;
		byte method_ids[] = new byte[num_methods];
		byte response[] = new byte[2];
		// 给个信号量 初始值为未发现是socks5
		boolean found = false;
		// 定义数组
		response[0] = (byte) 5; // SOCKS version
		response[1] = (byte) 0xFF; // Not found, we are pessimistic
		// 0xFF = 11111111
		int bread = 0; // bytes read so far
		while (bread < num_methods)
			// bread 随着读取不断增长
			bread += in.read(method_ids, bread, num_methods - bread);
		//
		for (int i = 0; i < num_methods; ++i)
			if (method_ids[i] == methodId) {
				// 当出现 socks对应即可判断此socks链接成功
				found = true;
				response[1] = (byte) methodId;
				break;
			}

		out.write(response);
		return found;
	}
}
