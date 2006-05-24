/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRssConfig
 */
public class MyTunesRssConfig {
    private String myPort;
    private String myLibraryXml;
    private String myPassword;
    private boolean myCheckUpdateOnStart;
    private boolean myAutoStartServer;

    public String getLibraryXml() {
        return myLibraryXml;
    }

    public void setLibraryXml(String libraryXml) {
        myLibraryXml = libraryXml;
    }

    public int getPasswordHash() {
        return myPassword != null ? myPassword.hashCode() : 0;
    }

    public String getPassword() {
        return myPassword;
    }

    public void setPassword(String password) {
        myPassword = password;
    }

    public String getPort() {
        return myPort;
    }

    public void setPort(String port) {
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

    public void load() {
        myPort = Preferences.userRoot().node("/de/codewave/mytunesrss").get("port", "8080");
        myLibraryXml = Preferences.userRoot().node("/de/codewave/mytunesrss").get("library", "");
        myPassword = Preferences.userRoot().node("/de/codewave/mytunesrss").get("authPassword", "");
        myCheckUpdateOnStart = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("checkUpdateOnStart", true);
        myAutoStartServer = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("autoStartServer", false);
    }

    public void save() {
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("port", myPort);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("library", myLibraryXml);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("authPassword", myPassword);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("checkUpdateOnStart", myCheckUpdateOnStart);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("autoStartServer", myAutoStartServer);
    }
}