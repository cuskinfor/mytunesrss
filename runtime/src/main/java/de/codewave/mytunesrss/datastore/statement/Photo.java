/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public class Photo {
    private String myId;
    private String myName;
    private String myFile;
    private long myDate;
    private String myImageHash;
    private long myLastImageUpdate;

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

    public String getFile() {
        return myFile;
    }

    public void setFile(String file) {
        myFile = file;
    }

    public long getDate() {
        return myDate;
    }

    public void setDate(long date) {
        myDate = date;
    }

    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        this.myImageHash = imageHash;
    }

    public long getLastImageUpdate() {
        return myLastImageUpdate;
    }

    public void setLastImageUpdate(long lastImageUpdate) {
        this.myLastImageUpdate = lastImageUpdate;
    }
}
