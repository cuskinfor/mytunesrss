/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.Album
 */
public class Album {
    private String myName;
    private int myArtistCount;
    private int myTrackCount;
    private String myArtist;
    private String myImageHash;
    private int myYear;

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(int artistCount) {
        myArtistCount = artistCount;
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

    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    public int getYear() {
        return myYear;
    }
    
    public void setYear(int year) {
        myYear = year;
    }
    
}