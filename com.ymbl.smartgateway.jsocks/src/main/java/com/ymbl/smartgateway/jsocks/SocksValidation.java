package com.ymbl.smartgateway.jsocks;

import com.xbsafe.socks.server.*;
import java.net.Socket;

/** Test file for UserPasswordAuthentictor */

public class SocksValidation implements UserValidation {
    String user, password;

    public SocksValidation(String user, String password) {
       this.user = user;
       this.password = password;
    }

    public boolean isUserValid(String user, String password, Socket s) {
       System.err.println("User:" + user + "\tPassword:" + password);
       System.err.println("Socket:" + s);
       return (user.equals(this.user) && password.equals(this.password));
    }
}
