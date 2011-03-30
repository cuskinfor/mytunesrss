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

    public PhotoAlbum(String id, String name, long firstDate, long lastDate) {
        myId = id;
        myName = name;
        myFirstDate = firstDate;
        myLastDate = lastDate;
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
}
