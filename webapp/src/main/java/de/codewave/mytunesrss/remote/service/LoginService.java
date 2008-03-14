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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginService {
    private static final Log LOG = LogFactory.getLog(LoginService.class);

    static {
        try {
            DIGEST = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create message digest.", e);
            }
        }
    }

    private static MessageDigest DIGEST;

    public String login(String username, String password, int sessionTimeoutMinutes) throws UnsupportedEncodingException, IllegalAccessException {
        User user = MyTunesRss.CONFIG.getUser(username);
        if (user != null) {
            byte[] passwordHash = MyTunesRss.MESSAGE_DIGEST.digest(password.getBytes("UTF-8"));
            if (Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive()) {
                MyTunesRssRemoteEnv.getRequest().getSession().setAttribute("remoteApiUser", user);
                String sid = new String(Hex.encodeHex(DIGEST.digest((MyTunesRssRemoteEnv.getRequest().getSession().getId() +
                        System.currentTimeMillis()).getBytes("UTF-8"))));
                MyTunesRssRemoteEnv.addSession(new Session(sid, user, sessionTimeoutMinutes * 60000));
                return sid;
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }
}
