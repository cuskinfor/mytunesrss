/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.io.*;

/**
 * de.codewave.mytunesrss.datastore.statement.Track
 */
public class Track {
    private String myId;
    private String myName;
    private String myAlbum;
    private String myArtist;
    private int myTime;
    private int myTrackNumber;
    private File myFile;

    public String getAlbum() {
        return myAlbum;
    }

    public String getDisplayAlbum() {
        return "n/a".equals(myAlbum) ? "(unknown)" : myAlbum;
    }

    public void setAlbum(String album) {
        myAlbum = album;
    }

    public String getArtist() {
        return myArtist;
    }

    public String getDisplayArtist() {
        return "n/a".equals(myArtist) ? "(unknown)" : myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    public File getFile() {
        return myFile;
    }

    public void setFile(File file) {
        myFile = file;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public String getName() {
        return myName;
    }

    public String getDisplayName() {
        return "n/a".equals(myName) ? "(unknown)" : myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getTime() {
        return myTime;
    }

    public void setTime(int time) {
        myTime = time;
    }

    public int getTrackNumber() {
        return myTrackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        myTrackNumber = trackNumber;
    }
}