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
      this.sock  = s;
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
    	 in = sock.getInputStream();
    	 out = sock.getOutputStream();

		 while(streamLoop) {
    		 len = in.read(buf);
    		 if(len < 8) {
    			 continue;
    		 }
    		 else {
				 int ipos = 0;
				 while(ipos < len-8) {
					 int datalen = buf[ipos]*(2^24) + buf[ipos+1]*(2^16) + buf[ipos+2]*(2^8) + buf[ipos+3];
					 int fd = buf[ipos+4]*(2^24) + buf[ipos+5]*(2^16) + buf[ipos+6]*(2^8) + buf[ipos+7];

					 if(datalen <= 8 && ipos+datalen > len) {
						 ipos += datalen;
						 continue;
					 }

					 GwproxyLink gLink = client_links.get(fd);

	    			 if(datalen == 40) {
	        			 byte[] stream_data = new byte[32];
	        			 System.arraycopy(buf, ipos+8, stream_data, 0, 32);
	    				 if(NGX_GWPROXY_CONNECTION_NEW_PRE.equals(new String(stream_data))) {
	    					 gLink = new GwproxyLink(fd);
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
       //ioe.printStackTrace();
     } finally {
		try {
	       streamLoop = false;
	       log("Aborting operation");
	       if(sock != null) sock.close();
	       log("Main thread(client->remote)stopped.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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


   class GwproxyLink implements Runnable{
	   public final static int GWLINK_INIT = 0;
	   public final static int GWLINK_CONNECT = 1;
	   public final static int GWLINK_RELEASE = 2;

	   public int stat = GWLINK_INIT;
	   int key;
	   Socket sock = null;
	   boolean loop = true;
	   Thread thread;

	   public GwproxyLink(int fd) {
		   key = fd;
	   }

	   public boolean startSession(byte[] b, int off, int len) throws IOException {
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

		   log(msg);

		   switch(msg.command) {
		   case Proxy.SOCKS_CMD_CONNECT:
			   Socket s;
			   ProxyMessage response = null;

			   if(proxy == null)
				   s = new Socket(msg.ip, msg.port);
			   else
				   s = new SocksSocket(proxy, msg.ip, msg.port);

			   sock = s;
			   log("Connected to " + s.getInetAddress() + ":" + s.getPort());

			   if(msg instanceof Socks5Message) {
				   response = new Socks5Message(Proxy.SOCKS_SUCCESS,
						   							s.getLocalAddress(),
						   							s.getLocalPort());
			   } else {
				   response = new Socks4Message(Socks4Message.REPLY_OK,
						   						s.getLocalAddress(),
						   						s.getLocalPort());
			   }

			   response.writeWithClientfd(out, key);
			   System.out.println("new connection, fd=" + key);
			   stat = GWLINK_CONNECT;

			   if(sock != null) {
				   thread = new Thread(this);
				   thread.start();
			   }
	        break;
	      }
		  return true;
	   }

	   public void endSession() throws IOException {
		   loop = false;
		   if(sock != null) {
			   sock.close();
		   }
		   if(thread != null) {
			   thread.interrupt();
		   }
		   System.out.println("connection remove, fd=" + key);
	   }

	   public void send(byte[] b, int off, int len) throws IOException {
		   if(sock != null) {
			   sock.getOutputStream().write(b, off, len);
		   }
	   }

	   public void run() {
		   try {
			   int len;
			   byte[] buf = new byte[BUF_SIZE];

			   while(loop) {
				   if(sock != null) {
					   len = sock.getInputStream().read(buf, 8, BUF_SIZE-8);
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
				   }
				   else {
					   loop = false;
				   }
			   }
			} catch (SocketException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
   }
}
