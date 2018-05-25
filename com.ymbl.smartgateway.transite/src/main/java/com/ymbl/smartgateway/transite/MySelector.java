package com.ymbl.smartgateway.transite;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.ymbl.smartgateway.transite.log.SystemLogger;

public class MySelector {
	public void startSelectListen(int port) {
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
	        Selector selector = Selector.open();
	        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	        while (true) {
	        	int n = selector.select();
	            if (n == 0) {
	                continue;
	            }

	            Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
	            while (ite.hasNext()) {
	            	SelectionKey key = ite.next();
	            	if (key.isAcceptable()) {
	            		ServerSocketChannel server = (ServerSocketChannel) key.channel();
	                    SocketChannel channel = server.accept();
	                    if (channel == null) {
	                        return;
	                    }
	                    Socket sock = channel.socket();
	                    SystemLogger.info("local: " + sock.getLocalAddress().toString() + ", "
	                    		+ sock.getLocalPort() + "; remote: " + sock.getRemoteSocketAddress().toString()
	                    		+ ", " + sock.getPort() + ", " + sock.getInetAddress().toString());
	                    channel.configureBlocking(false);
	                    channel.register(selector, SelectionKey.OP_READ);
	            	}
	            	else if (key.isReadable()) {
	                    int count;
	            		SocketChannel socketChannel = (SocketChannel) key.channel();
	            		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
	                    byteBuffer.order(ByteOrder.BIG_ENDIAN);
	                    byteBuffer.clear();

	                    while ((count = socketChannel.read(byteBuffer)) > 0) {
	                        byteBuffer.flip();
	                        while (byteBuffer.hasRemaining()) {
	                        	
	                            socketChannel.write(byteBuffer);
	                        }
	                        byteBuffer.clear();
	                    }

	                    if (count < 0) {
	                        socketChannel.close();
	                    }
	                }

	            	ite.remove();
	            }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
