/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public class Photo {
    private String myId;
    private String myName;
    private String myFile;
    private Long myDate;
    private String imageHash;
    private long lastImageUpdate;

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

    public Long getDate() {
        return myDate;
    }

    public void setDate(Long date) {
        myDate = date;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    public long getLastImageUpdate() {
        return lastImageUpdate;
    }

    public void setLastImageUpdate(long lastImageUpdate) {
        this.lastImageUpdate = lastImageUpdate;
    }
}
