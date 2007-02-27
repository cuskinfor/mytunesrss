/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.UserConfig
 */
public class UserConfig implements UserConfigMBean {
    private String myUsername;

    public UserConfig(String username) {
        myUsername = username;
    }
}