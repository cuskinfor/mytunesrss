/*
 * LoginService.java 30.01.2008
 *
 * Copyright (c) 2008 WEB.DE GmbH, Karlsruhe. All rights reserved.
 *
 * $Id$
 */
package de.codewave.mytunesrss.remote.service;

import java.io.*;
import java.util.*;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.remote.*;

public class LoginService {
    public boolean login(String username, String password) throws UnsupportedEncodingException {
        User user = MyTunesRss.CONFIG.getUser(username);
        if (user != null) {
            byte[] passwordHash = MyTunesRss.MESSAGE_DIGEST.digest(password.getBytes("UTF-8"));
            if (Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive()) {
                MyTunesRssRemoteEnv.getRequest().getSession().setAttribute("remoteApiUser", user);
                return true;
            }
        }
        MyTunesRssRemoteEnv.getRequest().getSession().removeAttribute("remoteApiUser");
        return false;
    }
}
