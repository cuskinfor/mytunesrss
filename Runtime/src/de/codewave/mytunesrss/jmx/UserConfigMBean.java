/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import javax.management.*;

/**
 * de.codewave.mytunesrss.jmx.UserConfigMBean
 */
public interface UserConfigMBean {
    void activate();
    void deactivate();
    boolean isActive();
    String getName();
    void setName(String name) throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException,
            InstanceAlreadyExistsException;
    void setPassword(String password);
}