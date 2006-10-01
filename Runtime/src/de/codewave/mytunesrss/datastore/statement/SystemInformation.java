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
    private String myItunesLibraryId;
    private String myBaseDirId;

    public String getItunesLibraryId() {
        return myItunesLibraryId;
    }

    public void setItunesLibraryId(String itunesLibraryId) {
        myItunesLibraryId = itunesLibraryId;
    }

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

    public String getBaseDirId() {
        return myBaseDirId;
    }

    public void setBaseDirId(String baseDirId) {
        myBaseDirId = baseDirId;
    }
}