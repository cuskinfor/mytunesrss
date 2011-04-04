/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public class PhotoAlbum {
    private String myId;
    private String myName;
    private Long myFirstDate;
    private Long myLastDate;

    public PhotoAlbum(String id, String name, Long firstDate, Long lastDate) {
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

    public Long getFirstDate() {
        return myFirstDate;
    }

    public Long getLastDate() {
        return myLastDate;
    }
}
