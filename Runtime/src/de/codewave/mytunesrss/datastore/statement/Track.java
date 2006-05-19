/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.io.*;
import java.net.*;

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

    public void setAlbum(String album) {
        myAlbum = album;
    }

    public String getArtist() {
        return myArtist;
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

    public String getContentType() {
        String name = getFile().getName().toLowerCase();
        if (name.endsWith(".mp3")) {
            return "audio/mp3";
        } else if (name.endsWith(".m4p") || name.endsWith(".m4a") || name.endsWith(".mp4")) {
            return "audio/mp4";
        }
        return URLConnection.guessContentTypeFromName(name);
    }

    public long getContentLength() {
        return getFile().length();
    }

    @Override
    public boolean equals(Object other) {
        if (getId() == null) {
            return ((Track)other).getId() == null;
        }
        return getId().equals(((Track)other).getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? 0 : getId().hashCode();
    }
}