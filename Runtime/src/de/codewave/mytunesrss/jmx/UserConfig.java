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
public class UserConfig extends MyTunesRssMBean implements UserConfigMBean {
  UserConfig() throws NotCompliantMBeanException {
    super(UserConfigMBean.class);
  }

  private static final Log LOG = LogFactory.getLog(UserConfig.class);

    public String addUser(String name, String password) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException,
            NotCompliantMBeanException, InstanceAlreadyExistsException {
        User user = new User(name);
        try {
            user.setPasswordHash(MyTunesRss.MESSAGE_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
            MyTunesRssJmxUtils.unregisterUsers();
            MyTunesRss.CONFIG.addUser(user);
            MyTunesRssJmxUtils.registerUsers();
            return MyTunesRss.BUNDLE.getString("ok");
        } catch (UnsupportedEncodingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create password hash.", e);
            }
            return e.getMessage();
        }
    }
}