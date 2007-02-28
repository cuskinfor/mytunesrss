/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import javax.management.*;

/**
 * de.codewave.mytunesrss.jmx.UserConfigMBean
 */
public interface UserConfigMBean {
    String addUser(String name, String password) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException,
            NotCompliantMBeanException, InstanceAlreadyExistsException;
}