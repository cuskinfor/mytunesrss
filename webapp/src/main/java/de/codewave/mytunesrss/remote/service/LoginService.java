/*
 * LoginService.java 30.01.2008
 *
 * Copyright (c) 2008 WEB.DE GmbH, Karlsruhe. All rights reserved.
 *
 * $Id$
 */
package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class LoginService {
    public String login(String username, String password, int sessionTimeoutMinutes) throws UnsupportedEncodingException, IllegalAccessException {
        User user = MyTunesRss.CONFIG.getUser(username);
        if (user != null) {
            byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8"));
            if (Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive()) {
                MyTunesRssRemoteEnv.getRequest().getSession().setAttribute("remoteApiUser", user);
                String sid = new String(Hex.encodeHex(MyTunesRss.MD5_DIGEST.digest((MyTunesRssRemoteEnv.getRequest().getSession().getId() +
                        System.currentTimeMillis()).getBytes("UTF-8"))));
                MyTunesRssRemoteEnv.addSession(new Session(sid, user, sessionTimeoutMinutes * 60000));
                return sid;
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    // todo: remote-api: testing
    public void logout() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            MyTunesRssRemoteEnv.getSession().invalidate();
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }
}
