/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SystemInformation
 */
public class SystemInformation {
    private long myLastUpdate = Long.MIN_VALUE;
    private String myVersion;

    public long getLastUpdate() {
        return myLastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        myLastUpdate = lastUpdate;
    }

    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
    }
}