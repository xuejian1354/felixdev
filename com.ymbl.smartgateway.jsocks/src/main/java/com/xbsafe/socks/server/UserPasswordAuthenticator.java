package com.xbsafe.socks.server;

//import com.xbsafe.socks.ProxyMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
  This class implements SOCKS5 User/Password authentication scheme as
  defined in rfc1929,the server side of it.
*/
public class UserPasswordAuthenticator extends ServerAuthenticatorNone{

   static final int METHOD_ID = 2;

   UserValidation validator;

   private byte[] client_fd = new byte[4];

   /**
    Construct a new UserPasswordAuthentication object, with given
    UserVlaidation scheme.

    @param v UserValidation to use for validating users.
   */
   public UserPasswordAuthenticator(UserValidation validator) {
      this.validator = validator;
   }

   public ServerAuthenticator startSession(Socket s) throws IOException {
     InputStream in = s.getInputStream();
     OutputStream out = s.getOutputStream();

     if(in.read() != 5) return null; //Drop non version 5 messages.

     if(!selectSocks5Authentication(in, out, METHOD_ID)) 
       return null;
     if(!doUserPasswordAuthentication(s, in, out))
       return null;

     return new ServerAuthenticatorNone(in, out);
   }

   public ServerAuthenticator startSessionWithClientfd(Socket s) throws IOException {
     InputStream in = s.getInputStream();
     OutputStream out = s.getOutputStream();

     in.read(client_fd, 0, 4);

     if(in.read() != 5) return null; //Drop non version 5 messages.

     if(!selectSocks5Authentication(in, out, METHOD_ID, client_fd)) 
       return null;
     if(!doUserPasswordAuthenticationWithClientfd(s, in, out))
       return null;

     return new ServerAuthenticatorNone(in, out);
   }

   static public boolean selectSocks5Authentication(InputStream in, 
										           OutputStream out,
										           int methodId,
										           byte[] client_fd)
										           		throws IOException {
	int num_methods = in.read();
	if (num_methods <= 0) return false;
	byte method_ids[] = new byte[num_methods];
	byte response[] = new byte[client_fd.length+2];
	boolean found = false;

	System.arraycopy(client_fd, 0, response, 0, client_fd.length);
	response[client_fd.length] = (byte) 5;    //SOCKS version
	response[client_fd.length+1] = (byte) 0xFF; //Not found, we are pessimistic

	int bread = 0; //bytes read so far
	while(bread < num_methods)
	  bread += in.read(method_ids, bread, num_methods-bread);

	for(int i=0; i < num_methods; ++i)
	  if(method_ids[i] == methodId) {
	    found = true;
	    response[client_fd.length+1] = (byte) methodId;
	    break;
	  }

	out.write(response);
	return found;
   }

   //Private Methods
   //////////////////

   private boolean doUserPasswordAuthentication(Socket s,
                                                InputStream in,
                                                OutputStream out)
                                                		throws IOException {
     int version = in.read();
     if(version != 1) return false;
     int ulen = in.read();
     if(ulen < 0) return false;
     byte[] user = new byte[ulen];
     in.read(user);
     int plen = in.read();
     if(plen < 0) return false;
     byte[] password = new byte[plen];
     in.read(password);

     if(validator.isUserValid(new String(user), new String(password), s)) {
       //System.out.println("user valid");
       out.write(new byte[]{1, 0});
     } else {
       //System.out.println("user invalid");
       out.write(new byte[]{1, 1});
       return false;
     }

     return true;
   }

   private boolean doUserPasswordAuthenticationWithClientfd(Socket s,
									           InputStream in,
									           OutputStream out)
									           		throws IOException {
	in.read(client_fd, 0, 4);
	int version = in.read();
	if(version != 1) return false;
	int ulen = in.read();
	if(ulen < 0) return false;
	byte[] user = new byte[ulen];
	in.read(user);
	int plen = in.read();
	if(plen < 0) return false;
	byte[] password = new byte[plen];
	in.read(password);
	byte response[] = new byte[client_fd.length+2];
	System.arraycopy(client_fd, 0, response, 0, client_fd.length);

	if(validator.isUserValid(new String(user), new String(password), s)) {
	  //System.out.println("user valid");
	  response[client_fd.length] = 1;
	  response[client_fd.length+1] = 0;
	  out.write(response);
	} else {
	  //System.out.println("user invalid");
	  response[client_fd.length] = 1;
	  response[client_fd.length+1] = 1;
	  out.write(response);
	  return false;
	}

	return true;
   }
}
