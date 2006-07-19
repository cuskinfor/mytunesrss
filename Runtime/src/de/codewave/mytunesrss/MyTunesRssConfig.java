/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import com.sun.java_cup.internal.*;

import java.util.prefs.*;
import java.io.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private static final Log LOG = LogFactory.getLog(MyTunesRssConfig.class);

    private int myPort;
    private String myLibraryXml;
    private String myPassword;
    private boolean myCheckUpdateOnStart;
    private boolean myAutoStartServer;
    private boolean myAutoUpdateDatabase;
    private int myAutoUpdateDatabaseInterval;
    private String myVersion;
    private boolean myIgnoreTimestamps;

    public String getLibraryXml() {
        return myLibraryXml;
    }

    public void setLibraryXml(String libraryXml) {
        myLibraryXml = libraryXml;
    }

    public byte[] getPasswordHash() {
        try {
            return myPassword != null ? MyTunesRss.MESSAGE_DIGEST.digest(myPassword.getBytes("UTF-8")) : null;
        } catch (UnsupportedEncodingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(null, e);
            }
        }
        return null;
    }

    public String getPassword() {
        return myPassword;
    }

    public void setPassword(String password) {
        myPassword = password;
    }

    public int getPort() {
        return myPort;
    }

    public void setPort(int port) {
        myPort = port;
    }

    public boolean isCheckUpdateOnStart() {
        return myCheckUpdateOnStart;
    }

    public void setCheckUpdateOnStart(boolean checkUpdateOnStart) {
        myCheckUpdateOnStart = checkUpdateOnStart;
    }

    public boolean isAutoStartServer() {
        return myAutoStartServer;
    }

    public void setAutoStartServer(boolean autoStartServer) {
        myAutoStartServer = autoStartServer;
    }

    public boolean isAutoUpdateDatabase() {
        return myAutoUpdateDatabase;
    }

    public void setAutoUpdateDatabase(boolean autoUpdateDatabase) {
        myAutoUpdateDatabase = autoUpdateDatabase;
    }

    public int getAutoUpdateDatabaseInterval() {
        return myAutoUpdateDatabaseInterval;
    }

    public void setAutoUpdateDatabaseInterval(int autoUpdateDatabaseInterval) {
        myAutoUpdateDatabaseInterval = autoUpdateDatabaseInterval;
    }

    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
    }

    public boolean isIgnoreTimestamps() {
        return myIgnoreTimestamps;
    }

    public void setIgnoreTimestamps(boolean ignoreTimestamps) {
        myIgnoreTimestamps = ignoreTimestamps;
    }

    public void load() {
        checkPrefsVersion();
        myPort = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("serverPort", 8080);
        myLibraryXml = Preferences.userRoot().node("/de/codewave/mytunesrss").get("iTunesLibrary", "");
        myPassword = Preferences.userRoot().node("/de/codewave/mytunesrss").get("serverPassword", "");
        myCheckUpdateOnStart = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("checkUpdateOnStart", true);
        myAutoStartServer = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("autoStartServer", false);
        myAutoUpdateDatabase = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("autoUpdateDatabase", false);
        myAutoUpdateDatabaseInterval = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("autoUpdateDatabaseInterval", 600);
        myVersion = Preferences.userRoot().node("/de/codewave/mytunesrss").get("version", "");
        myIgnoreTimestamps = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("ignoreTimestamps", false);
    }

    public void save() {
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("version", MyTunesRss.VERSION);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("serverPort", myPort);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("iTunesLibrary", myLibraryXml);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("serverPassword", myPassword);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("checkUpdateOnStart", myCheckUpdateOnStart);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("autoStartServer", myAutoStartServer);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("autoUpdateDatabase", myAutoUpdateDatabase);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("autoUpdateDatabaseInterval", myAutoUpdateDatabaseInterval);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("version", myVersion);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("ignoreTimestamps", myIgnoreTimestamps);
    }

    private void checkPrefsVersion() {
        String version = Preferences.userRoot().node("/de/codewave/mytunesrss").get("version", "");
        if ("".equals(version)) {
            try {
                Preferences.userRoot().node("/de/codewave/mytunesrss").removeNode();
            } catch (BackingStoreException e) {
                // intentionally left blank
            }
        }
    }
}