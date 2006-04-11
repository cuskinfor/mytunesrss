/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.lang.*;

import java.io.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.PrefData
 */
public class PrefData {
    public static PrefData getSavedPrefData() {
        PrefData prefData = new PrefData();
        prefData.load();
        return prefData;
    }

    private String myPort;
    private String myLibraryXml;
    private boolean myAuth;
    private String myUsername;
    private String myPassword;
    private String myFakeMp3Suffix;
    private String myFakeM4aSuffix;

    public boolean isAuth() {
        return myAuth;
    }

    public void setAuth(boolean auth) {
        myAuth = auth;
    }

    public String getFakeM4aSuffix() {
        return myFakeM4aSuffix;
    }

    public void setFakeM4aSuffix(String fakeM4aSuffix) {
        myFakeM4aSuffix = fakeM4aSuffix;
    }

    public String getFakeMp3Suffix() {
        return myFakeMp3Suffix;
    }

    public void setFakeMp3Suffix(String fakeMp3Suffix) {
        myFakeMp3Suffix = fakeMp3Suffix;
    }

    public String getLibraryXml() {
        return myLibraryXml;
    }

    public void setLibraryXml(String libraryXml) {
        myLibraryXml = libraryXml;
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

    public String getUsername() {
        return myUsername;
    }

    public void setUsername(String username) {
        myUsername = username;
    }

    public void load() {
        myPort = Preferences.userRoot().node("/de/codewave/mytunesrss").get("port", "8080");
        myLibraryXml = Preferences.userRoot().node("/de/codewave/mytunesrss").get("library", "");
        myAuth = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("useAuth", true);
        myUsername = Preferences.userRoot().node("/de/codewave/mytunesrss").get("authUsername", "");
        myPassword = Preferences.userRoot().node("/de/codewave/mytunesrss").get("authPassword", "");
        myFakeMp3Suffix = Preferences.userRoot().node("/de/codewave/mytunesrss").get("fakeMp3Suffix", "");
        myFakeM4aSuffix = Preferences.userRoot().node("/de/codewave/mytunesrss").get("fakeM4aSuffix", "mp4");
    }

    public void save() {
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("port", myPort);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("library", myLibraryXml);
        Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("useAuth", myAuth);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("authUsername", myUsername);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("authPassword", myPassword);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("fakeMp3Suffix", myFakeMp3Suffix);
        Preferences.userRoot().node("/de/codewave/mytunesrss").put("fakeM4aSuffix", myFakeM4aSuffix);
    }

    public boolean isDiffenrentFromSaved() {
        return !equals(getSavedPrefData());
    }

    @Override
    public int hashCode() {
        // todo: implement method
        throw new UnsupportedOperationException("method hashCode of class PrefData is not yet implemented!");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PrefData) {
            PrefData other = (PrefData)obj;
            boolean result = StringUtils.equals(getPort(), other.getPort());
            result &= StringUtils.equals(getLibraryXml(), other.getLibraryXml());
            result &= isAuth() == other.isAuth();
            result &= StringUtils.equals(getUsername(), other.getUsername());
            result &= StringUtils.equals(getPassword(), other.getPassword());
            result &= StringUtils.equals(getFakeMp3Suffix(), other.getFakeMp3Suffix());
            result &= StringUtils.equals(getFakeM4aSuffix(), other.getFakeM4aSuffix());
            return result;
        }
        return false;
    }
}