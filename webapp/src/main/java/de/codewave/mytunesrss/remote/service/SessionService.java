/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

public class SessionService {

    public void setActiveTranscoders(String[] transcoderNames) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            MyTunesRssRemoteEnv.getSession().setAttribute("activateTranscoderNames", transcoderNames);
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }
}
