package com.xbsafe.socks;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class ProxyClient implements Runnable {
	
   public final static String NGX_GWPROXY_CONNECTION_NEW_PRE = "JFLS#%^Fs&XK*HJGTT&$#@!S$L:ZXRLC";
   public final static String NGX_GWPROXY_CONNECTION_NEW_SUF = "NBID*^4>BC{j&t#5PK!FLSi7^9HCBO%U";

   ProxyMessage msg = null;

   Socket sock = null;
   InputStream in;
   OutputStream out;

   static final int BUF_SIZE	= 8192;

   long lastReadTime;

   boolean streamLoop = true;

   static int iddleTimeout	= 180000; //3 minutes

   static PrintStream log = null;
   static Proxy proxy;

   Map<Integer, GwproxyLink> client_links = new HashMap<Integer, GwproxyLink>();

   //Constructors
   ////////////////////
   private ProxyClient(Socket s) {
 	  try {
 		 this.sock  = s;
 		 in = sock.getInputStream();
 		 out = sock.getOutputStream();
	  } catch (IOException e) {
		e.printStackTrace();
	  }
   }

   public static ProxyClient ProxyServerAsClient(Socket s) {
	   ProxyClient pc = new ProxyClient(s);
       (new Thread(pc)).start();
       return pc;
   }

   //Public methods
   /////////////////

   /**
     Set the logging stream. Specifying null disables logging.
   */
   public static void setLog(OutputStream out) {
      if(out == null) {
        log = null;
      } else {
        log = new PrintStream(out, true);
      }
   }

   /**
    Sets the timeout for connections, how long shoud server wait
    for data to arrive before dropping the connection.<br>
    Zero timeout implies infinity.<br>
    Default timeout is 3 minutes.
   */
   public static void setIddleTimeout(int timeout) {
      iddleTimeout = timeout;
   }

   //Runnable interface
   ////////////////////
   public void run() {
     try {
    	 int len;
		 byte[] buf = new byte[BUF_SIZE];

		 while(streamLoop) {
    		 len = in.read(buf);
    		 if(len < 8) {
    			 continue;
    		 }
    		 else {
				 int ipos = 0;
				 while(ipos < len-8) {
					 int datalen = (buf[ipos]&0xff)*(2^24) + (buf[ipos+1]&0xff)*(2^16) + (buf[ipos+2]&0xff)*(2^8) + (buf[ipos+3]&0xff);
					 int fd = (buf[ipos+4]&0xff)*(2^24) + (buf[ipos+5]*0xff)*(2^16) + (buf[ipos+6]*0xff)*(2^8) + (buf[ipos+7]&0xff);

					 if(datalen <= 8 && ipos+datalen > len) {
						 ipos += datalen;
						 continue;
					 }

					 System.out.println("===> datalen=" + datalen + ", fd=" + fd);
					 /*for (int i = 8; i < datalen; i++) {
						System.out.print(Integer.toHexString((buf[ipos+i]&0xff)).toUpperCase() + " ");
					 }
					 System.out.println();*/

					 GwproxyLink gLink = client_links.get(fd);

	    			 if(datalen == 40) {
	        			 byte[] stream_data = new byte[32];
	        			 System.arraycopy(buf, ipos+8, stream_data, 0, 32);
	    				 if(NGX_GWPROXY_CONNECTION_NEW_PRE.equals(new String(stream_data))) {
	    					 gLink = new GwproxyLink(fd){
	    						@Override
	    						public void SocketConnectDstFailed(int fd) {
	    							// TODO Auto-generated method stub
	    							client_links.remove(fd);
	    						}
	    					 };
	    					 client_links.put(fd, gLink);
	    					 ipos += datalen;
	    					 continue;
	        			 }
	        			 else if(NGX_GWPROXY_CONNECTION_NEW_SUF.equals(new String(stream_data))) {
	        				 if(gLink != null) {
	        					 gLink.stat = GwproxyLink.GWLINK_RELEASE;
	        					 client_links.remove(fd);
	        				 }
	        			 }
	    			 }

	    			 if(gLink != null) {
	    				switch (gLink.stat) {
						case GwproxyLink.GWLINK_INIT:
							if(!gLink.startSession(buf, ipos+8, datalen-8)) {
								client_links.remove(fd);
							}
							break;

						case GwproxyLink.GWLINK_CONNECT:
							gLink.send(buf, ipos+8, datalen-8);
							break;

						case GwproxyLink.GWLINK_RELEASE:
							gLink.endSession();
							break;
						}
	    			 }

	    			 ipos += datalen;
				 }
    		 }
		 }
     } catch(IOException ioe) {
       handleException(ioe);
       ioe.printStackTrace();
       streamLoop = false;
       log("Aborting operation");
       log("Main thread(client->remote)stopped.");
     }
   }

   private void handleException(IOException ioe) {
      //If we couldn't read the request, return;
      if(msg == null) return;
      int error_code = Proxy.SOCKS_FAILURE;

      if(ioe instanceof SocksException)
          error_code = ((SocksException)ioe).errCode;
      else if(ioe instanceof NoRouteToHostException)
          error_code = Proxy.SOCKS_HOST_UNREACHABLE;
      else if(ioe instanceof ConnectException)
          error_code = Proxy.SOCKS_CONNECTION_REFUSED;
      else if(ioe instanceof InterruptedIOException)
          error_code = Proxy.SOCKS_TTL_EXPIRE;

      if(error_code > Proxy.SOCKS_ADDR_NOT_SUPPORTED || error_code < 0) {
          error_code = Proxy.SOCKS_FAILURE; 
      }

      sendErrorMessage(error_code);
   }


   //Private methods
   //////////////////
   private void sendErrorMessage(int error_code) {
      ProxyMessage err_msg;
      if(msg instanceof Socks4Message)
         err_msg = new Socks4Message(Socks4Message.REPLY_REJECTED);
      else
         err_msg = new Socks5Message(error_code);
      try {
         err_msg.write(out);
      } catch(IOException ioe){}
   }

   static final void log(String s) {
     if(log != null) {
       log.println(s);
       log.flush();
     }
   }

   static final void log(ProxyMessage msg) {
      log("Request version:" + msg.version +
          "\tCommand: " + command2String(msg.command));
      log("IP:" + msg.ip + "\tPort:" + msg.port +
         (msg.version == 4 ? "\tUser:" + msg.user : ""));
   }

   static final String command_names[] = {"CONNECT", "BIND", "UDP_ASSOCIATE"};

   static final String command2String(int cmd) {
      if(cmd > 0 && cmd < 4) return command_names[cmd-1];
      else return "Unknown Command " + cmd;
   }


   abstract class GwproxyLink {
	   public final static int GWLINK_INIT = 0;
	   public final static int GWLINK_CONNECT = 1;
	   public final static int GWLINK_RELEASE = 2;

	   public int stat = GWLINK_INIT;
	   int key;
	   Socket msock = null;
	   boolean tryConn = true;
	   boolean loop = true;
	   byte[] tmpbuf = new byte[0];
	   Thread thread;

	   public GwproxyLink(int fd) {
		   key = fd;
	   }

	   synchronized public boolean startSession(byte[] b, int off, int len) {
		   if(!tryConn) {
			   byte[] tbuf = new byte[tmpbuf.length+len];
			   System.arraycopy(tmpbuf, 0, tbuf, 0, tmpbuf.length);
			   System.arraycopy(b, off, tbuf, tmpbuf.length, len);
			   tmpbuf = tbuf;
			   return true;
		   }

		   if(len < 8) {
			   return false;
		   }

		   int version = b[off];
		   if(version == 5) {
			   msg = new Socks5Message(b, off, len);
		   }
		   else {
			return false;
		   }

		   if (msg.ip == null) {
			   return false;
		   }

		   log(msg);

		   switch(msg.command) {
		   case Proxy.SOCKS_CMD_CONNECT:
			   tryConn = false;
			   new Thread(){
				   public void run() {
					   try {
						   synchronized (this) {
							   sleep(400);
						   }
						   Socket s = new Socket(msg.ip, msg.port);
						   msock = s;
						   log("Connected to " + s.getInetAddress() + ":" + s.getPort());

						   ProxyMessage response = new Socks5Message(Proxy.SOCKS_SUCCESS,
								   							s.getLocalAddress(),
								   							s.getLocalPort());

						   synchronized (this) {
							   response.writeWithClientfd(out, key);
						   }
						   System.out.println("new connection, fd=" + key);
						   stat = GWLINK_CONNECT;

						   if(msock != null) {
							   thread = new Thread(){
								   public void run() {
									   SockRecvRun();
								   };
							   };
							   thread.start();
							   if(tmpbuf.length > 0) {
								   msock.getOutputStream().write(tmpbuf);
							   }
						   }
					   } catch(ConnectException e) {
						   SocketConnectDstFailed(key);
						   e.printStackTrace();
					   } catch(SocketException e) {
						   SocketConnectDstFailed(key);
						   e.printStackTrace();
					   } catch (IOException e) {
						   SocketConnectDstFailed(key);
						   e.printStackTrace();
					   } catch (InterruptedException e) {
						   e.printStackTrace();
					   }
				   };
			   }.start();
	        break;
	      }

		  return true;
	   }

	   public void endSession() {
		   try {
			   loop = false;
			   if(msock != null) {
				   msock.close();
			   }
			   if(thread != null) {
				   thread.interrupt();
			   }
			   System.out.println("connection remove, fd=" + key);
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
	   }

	   public void send(byte[] b, int off, int len) {
		   try {
			   if(msock != null) {
				   msock.getOutputStream().write(b, off, len);
			   }
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
	   }

	   synchronized public void SockRecvRun() {
		   try {
			   int len;
			   byte[] buf = new byte[BUF_SIZE];

			   while(loop) {
				   if(msock != null) {
					   len = msock.getInputStream().read(buf, 8, BUF_SIZE-8);
					   int reslen = len + 8;
					   buf[0] = (byte)((reslen >> 24) & 0xFF);
					   buf[1] = (byte)((reslen >> 16) & 0xFF);
					   buf[2] = (byte)((reslen >> 8) & 0xFF);
					   buf[3] = (byte)((reslen) & 0xFF);
					   buf[4] = (byte)((key >> 24) & 0xFF);
					   buf[5] = (byte)((key >> 16) & 0xFF);
					   buf[6] = (byte)((key >> 8) & 0xFF);
					   buf[7] = (byte)((key) & 0xFF);
					   out.write(buf, 0, reslen);

					   System.out.println("<=== datalen=" + reslen + ", fd=" + key);
					   /*for (int i = 8; i < reslen; i++) {
						   System.out.print(Integer.toHexString((buf[i]&0xff)).toUpperCase() + " ");
					   }
					   System.out.println();*/
				   }
				   else {
					   loop = false;
					   System.out.println("end recv for...");
				   }
			   }
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }

	   public abstract void SocketConnectDstFailed(int fd);
   }
}
