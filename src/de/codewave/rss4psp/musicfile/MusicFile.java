/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.rss4psp.musicfile;

import java.io.*;

/**
 * de.codewave.rss4psp.musicfile.MusicFile
 */
public class MusicFile implements Serializable {
    private String myAlbum;
    private String myArtist;
    private File myFile;
    private String myName;
    private String myId;
    private int myTrackNumber;
    private String myVirtualFileName;

    public String getAlbum() {
        return myAlbum;
    }

    public synchronized void setAlbum(String album) {
        myAlbum = album;
        myVirtualFileName = null;
    }

    public String getArtist() {
        return myArtist;
    }

    public synchronized void setArtist(String artist) {
        myArtist = artist;
        myVirtualFileName = null;
    }

    public File getFile() {
        return myFile;
    }

    public long getFileLength() {
        return myFile != null && myFile.exists() && myFile.isFile() ? myFile.length() : 0;
    }

    public void setFile(File file) {
        myFile = file;
    }

    public String getName() {
        return myName;
    }

    public synchronized void setName(String name) {
        myName = name;
        myVirtualFileName = null;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public int getTrackNumber() {
        return myTrackNumber;
    }

    public String getTextualTrackNumber() {
        if (myTrackNumber > 9) {
            return "" + myTrackNumber;
        } else if (myTrackNumber > 0) {
            return "0" + myTrackNumber;
        }
        return null;
    }

    public synchronized void setTrackNumber(int trackNumber) {
        myTrackNumber = trackNumber;
        myVirtualFileName = null;
    }

    @Override
    public String toString() {
        return String.format("Title '%s' on album '%s' (track %d) by artist '%s' with id '%s' can be found at '%s'",
                             myName,
                             myAlbum,
                             myTrackNumber,
                             myArtist,
                             myId,
                             myFile.getAbsolutePath());
    }

    public boolean isComplete() {
        return (myName != null && myArtist != null && myId != null && myAlbum != null && myFile.getAbsolutePath().toLowerCase().endsWith(".mp3"));
    }

    public synchronized String getVirtualFileName() {
        if (myVirtualFileName == null) {
            myVirtualFileName = myArtist + " - " + myAlbum + " - ";
            String trackNumber = getTextualTrackNumber();
            if (trackNumber != null) {
                myVirtualFileName += trackNumber + " - ";
            }
            myVirtualFileName += myName + ".mp3";
            myVirtualFileName = myVirtualFileName.replace(' ', '_');
        }
        return myVirtualFileName;
    }
}