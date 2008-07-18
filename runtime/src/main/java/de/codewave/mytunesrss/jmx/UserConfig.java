/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.UnsupportedEncodingException;

/**
 * de.codewave.mytunesrss.jmx.UserConfig
 */
public class UserConfig extends MyTunesRssMBean implements UserConfigMBean {
  UserConfig() throws NotCompliantMBeanException {
    super(UserConfigMBean.class);
  }

  private static final Logger LOG = LoggerFactory.getLogger(UserConfig.class);

    public String addUser(String name, String password) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException,
            NotCompliantMBeanException, InstanceAlreadyExistsException {
        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(password)) {
        User user = new User(name);
        try {
            user.setPasswordHash(MyTunesRss.SHA1_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
            MyTunesRssJmxUtils.unregisterUsers();
            MyTunesRss.CONFIG.addUser(user);
            MyTunesRssJmxUtils.registerUsers();
            onChange();
            return MyTunesRssUtils.getBundleString("ok");
        } catch (UnsupportedEncodingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create password hash.", e);
            }
            return e.getMessage();
        }} else {
            return MyTunesRssUtils.getBundleString("jmx.newUserMissingNameOrPassword");
        }
    }
}