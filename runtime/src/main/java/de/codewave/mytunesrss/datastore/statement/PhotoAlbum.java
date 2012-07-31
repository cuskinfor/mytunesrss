/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public class PhotoAlbum {
    private String myId;
    private String myName;
    private long myFirstDate;
    private long myLastDate;
    private int myPhotoCount;

    public PhotoAlbum(String id, String name, long firstDate, long lastDate, int photoCount) {
        myId = id;
        myName = name;
        myFirstDate = firstDate;
        myLastDate = lastDate;
        myPhotoCount = photoCount;
    }

    public String getId() {
        return myId;
    }

    public String getName() {
        return myName;
    }

    public long getFirstDate() {
        return myFirstDate;
    }

    public long getLastDate() {
        return myLastDate;
    }

    public int getPhotoCount() {
        return myPhotoCount;
    }
}
