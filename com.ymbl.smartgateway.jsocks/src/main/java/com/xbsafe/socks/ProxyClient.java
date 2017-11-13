package com.xbsafe.socks;

import com.xbsafe.socks.server.ServerAuthenticator;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class ProxyClient implements Runnable {
	
   public final static String NGX_GWPROXY_CONNECTION_NEW_PRE = "JFLS#%^Fs&XK*HJGTT&$#@!S$L:ZXRLC";
   public final static String NGX_GWPROXY_CONNECTION_NEW_SUF = "NBID*^4>BC{j&t#5PK!FLSi7^9HCBO%U";

   ServerAuthenticator auth;
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

   byte[] tfd = new  byte[4];
   Map<Integer, GwproxyLink> client_links = new HashMap<Integer, GwproxyLink>();

   //Constructors
   ////////////////////
   private ProxyClient(ServerAuthenticator auth, Socket s) {
      this.auth  = auth;
      this.sock  = s;
   }

   public static ProxyClient ProxyServerAsClient(ServerAuthenticator auth, Socket s) {
	   ProxyClient pc = new ProxyClient(auth, s);
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
    		 if(len < 4) {
    			 continue;
    		 }
    		 else {
    			 byte[] tcli_fd = new byte[4];
    			 System.arraycopy(buf, 0, tcli_fd, 0, 4);

				 int fd = tcli_fd[0]*(2^24) + tcli_fd[1]*(2^16) + tcli_fd[2]*(2^8) + tcli_fd[3];

    			 if(len == 36) {
        			 byte[] stream_data = new byte[len-4];
        			 System.arraycopy(buf, 4, stream_data, 0, len-4);
    				 if(NGX_GWPROXY_CONNECTION_NEW_PRE.equals(new String(stream_data))) {
        				 startSession(fd);
        				 continue;
        			 }
        			 else if(NGX_GWPROXY_CONNECTION_NEW_SUF.equals(new String(stream_data))) {
        				 endSession(fd);
        				 continue;
        			 }
    			 }

    			 GwproxyLink gLink = client_links.get(fd);
    			 if(gLink != null) {
    				 gLink.getOutputstream().write(buf, 4, len-4);
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
	       if(auth != null) auth.endSession();
	       log("Main thread(client->remote)stopped.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
   }

   //Private methods
   /////////////////
   private void startSession(final int fd) {
	   try {
		  PushbackInputStream push_in;
		  ProxyMessage msg;

		  if(auth.startSessionWithClientfd(sock) == null) { //Authentication failed
		    log("Authentication failed");
		    return;
	      }

		  in.read(tfd, 0, 4);

	      if(in instanceof PushbackInputStream)
	         push_in = (PushbackInputStream) in;
	      else
	         push_in = new PushbackInputStream(in);

	      int version = push_in.read();
	      push_in.unread(version);

	      if(version == 5) {
	        msg = new Socks5Message(push_in, false);
	      } else if(version == 4) {
	        msg = new Socks4Message(push_in, false);
	      } else {
	        throw new SocksException(Proxy.SOCKS_FAILURE);
	      }

	      if(!auth.checkRequest(msg))
	    	  throw new SocksException(Proxy.SOCKS_FAILURE);

	      if(msg.ip == null) {
	        if(msg instanceof Socks5Message) {
	          msg.ip = InetAddress.getByName(msg.host);
	        } else
	          throw new SocksException(Proxy.SOCKS_FAILURE);
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
	            response.writeWithClientfd(out, tfd);

	            client_links.put(fd, new GwproxyLink(s, new Thread(){
	            	@Override
	            	public void run() {
	            		// TODO Auto-generated method stub
	            		int len;
	           		 	byte[] buf = new byte[BUF_SIZE];
	           		 	System.arraycopy(tfd, 0, buf, 0, 4);

	        			try {
		            		GwproxyLink gLink = client_links.get(fd);
							InputStream gin = gLink.getInputStream();
		            		while(gLink.getLoop()) {
		            			len = gin.read(buf, 4, BUF_SIZE-4);
		            			out.write(buf, 0, len+4);
		            		}
						} catch (SocketException e) {
							// TODO Auto-generated catch block
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	}
	            }));
	     	    System.out.println("new connection, fd=" + fd);
	        break;

	        default:
	          throw new SocksException(Proxy.SOCKS_CMD_NOT_SUPPORTED);
	      }
	   } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	   }
   }

   private void endSession(int fd) throws IOException {
	   GwproxyLink gLink = client_links.get(fd);
	   if(gLink != null) {
		   gLink.ReleaseLink();
	   }
	   client_links.remove(fd);
	   System.out.println("connection remove, fd=" + fd);
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
   
   class GwproxyLink {
	   boolean loop;
	   Socket sock;
	   Thread thread;

	   public GwproxyLink(Socket s, Thread t) {
		 // TODO Auto-generated constructor stub
		 sock = s;
		 thread = t;
		 loop = true;
		 if (thread != null) {
			thread.start();
		 }
	   }

	   public boolean getLoop() {
		   return loop;
	   }

	   public OutputStream getOutputstream() throws IOException {
		   return sock.getOutputStream();
	   }

	   public InputStream getInputStream() throws IOException {
		   return sock.getInputStream();
	   }

	   public void ReleaseLink() throws IOException {
		   loop = false;
		   sock.close();
		   thread.interrupt();
	   }
   }
}
