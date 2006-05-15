/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.Album
 */
public class Album {
    private String myName;
    private boolean myVarious;
    private int myTrackCount;
    private String myArtist;

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public boolean isVarious() {
        return myVarious;
    }

    public void setVarious(boolean various) {
        myVarious = various;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }

    public String getArtist() {
        return myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }
}