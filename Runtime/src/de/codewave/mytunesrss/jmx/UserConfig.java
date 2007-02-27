/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.management.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.jmx.UserConfig
 */
public class UserConfig implements UserConfigMBean {
    private static final Log LOG = LogFactory.getLog(UserConfig.class);

    private String myUsername;

    public UserConfig(String username) {
        myUsername = username;
    }

    public void activate() {
        MyTunesRss.CONFIG.getUser(myUsername).setActive(true);
    }

    public void deactivate() {
        MyTunesRss.CONFIG.getUser(myUsername).setActive(false);
    }

    public boolean isActive() {
        return MyTunesRss.CONFIG.getUser(myUsername).isActive();
    }

    public String getName() {
        return MyTunesRss.CONFIG.getUser(myUsername).getName();
    }

    public void setName(String name) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException,
            NotCompliantMBeanException, InstanceAlreadyExistsException {
        MyTunesRssJmxUtils.unregisterUsers();
        MyTunesRss.CONFIG.getUser(myUsername).setName(name);
        MyTunesRssJmxUtils.registerUsers();
    }

    public void setPassword(String password) {
        try {
            MyTunesRss.CONFIG.getUser(myUsername).setPasswordHash(MyTunesRss.MESSAGE_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create password hash.", e);
            }
        }
    }
}