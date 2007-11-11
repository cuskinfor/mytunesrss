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
    private int myTrackCount;
    private int myAlbumCount;
    private int myArtistCount;
    private int myGenreCount;

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

    public int getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(int albumCount) {
        myAlbumCount = albumCount;
    }

    public int getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(int artistCount) {
        myArtistCount = artistCount;
    }

    public int getGenreCount() {
        return myGenreCount;
    }

    public void setGenreCount(int genreCount) {
        myGenreCount = genreCount;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }
}