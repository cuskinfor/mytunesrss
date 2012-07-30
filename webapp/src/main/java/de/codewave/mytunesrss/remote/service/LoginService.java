/*
 * LoginService.java 30.01.2008
 *
 * Copyright (c) 2008 WEB.DE GmbH, Karlsruhe. All rights reserved.
 *
 * $Id$
 */
package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class LoginService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    public String login(String username, String password, int sessionTimeoutMinutes) throws UnsupportedEncodingException, IllegalAccessException {
        User user = MyTunesRss.CONFIG.getUser(username);
        if (user != null) {
            byte[] passwordHash1 = MyTunesRss.SHA1_DIGEST.get().digest(password.getBytes("UTF-8"));
            byte[] passwordHash2 = new byte[0];
            try {
                passwordHash2 = Hex.decodeHex(password.toCharArray());
            } catch (DecoderException e) {
                LOGGER.debug("Could not decode hex value \"" + password + "\".");
            }
            if ((Arrays.equals(user.getPasswordHash(), passwordHash1) || Arrays.equals(user.getPasswordHash(), passwordHash2)) && user.isActive()) {
                return handleLogin(user, sessionTimeoutMinutes);
            }
        }
        if (MyTunesRssUtils.loginLDAP(username, password)){
            return handleLogin(user, sessionTimeoutMinutes);
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }

    private String handleLogin(User user, int sessionTimeoutMinutes) {
        MyTunesRssRemoteEnv.getRequest().getSession().setAttribute("remoteApiUser", user);
        String sid = MyTunesRssRemoteEnv.createSessionId();
        MyTunesRssRemoteEnv.addSession(MyTunesRssRemoteEnv.getRequest(), new Session(sid, user, sessionTimeoutMinutes * 60000));
        return sid;
    }

    public void logout() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            MyTunesRssRemoteEnv.getSession().invalidate();
        } else {
            throw new IllegalAccessException("UNAUTHORIZED");
        }
    }

    public boolean ping() {
        return MyTunesRssRemoteEnv.getSession() != null && MyTunesRssRemoteEnv.getSession().getUser() != null;
    }

    public Object getUserInfo() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return RenderMachine.getInstance().render(user);
        } else {
            throw new IllegalAccessException("UNAUTHORIZED");
        }
    }

    public void saveUserSettings(String password, String email, String lastFmUser, String lastFmPassword)
            throws IllegalAccessException, UnsupportedEncodingException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            if (user.isChangeEmail() && StringUtils.isNotBlank(email)) {
                user.setEmail(StringUtils.trim(email));
            }
            if (user.isChangePassword() && StringUtils.isNotBlank(password)) {
                user.setPasswordHash(MyTunesRss.SHA1_DIGEST.get().digest(StringUtils.trim(password).getBytes("UTF-8")));
            }
            if (user.isEditLastFmAccount()) {
                if (StringUtils.isNotBlank(lastFmUser)) {
                    user.setLastFmUsername(StringUtils.trim(lastFmUser));
                }
                if (StringUtils.isNotBlank(lastFmPassword)) {
                    user.setLastFmPasswordHash(MyTunesRss.MD5_DIGEST.get().digest(StringUtils.trim(lastFmPassword).getBytes("UTF-8")));
                }
            }
        } else {
            throw new IllegalAccessException("UNAUTHORIZED");
        }
    }
}
