/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

/**
 * de.codewave.mytunesrss.datastore.Artist
 */
public class Artist {
    private String myName;
    private int myAlbumCount;
    private int myTrackCount;

    public int getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(int albumCount) {
        myAlbumCount = albumCount;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }
}