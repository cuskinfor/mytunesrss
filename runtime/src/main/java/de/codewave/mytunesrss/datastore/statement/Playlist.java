/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.Playlist
 */
public class Playlist {
    private String myId;
    private String myName;
    private PlaylistType myType;
    private int myTrackCount;
    private boolean myUserPrivate;
    private boolean myHidden;
    private String myUserOwner;

    public Playlist() {
        // intentionally left blank
    }

    public Playlist(String id, PlaylistType type, String name, int trackCount) {
        myId = id;
        myType = type;
        myName = name;
        myTrackCount = trackCount;
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

    public PlaylistType getType() {
        return myType;
    }

    public void setType(PlaylistType type) {
        myType = type;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }

    public boolean isUserPrivate() {
        return myUserPrivate;
    }

    public void setUserPrivate(boolean userPrivate) {
        myUserPrivate = userPrivate;
    }

    public String getUserOwner() {
        return myUserOwner;
    }

    public void setUserOwner(String userOwner) {
        myUserOwner = userOwner;
    }

    public boolean isHidden() {
        return myHidden;
    }

    public void setHidden(boolean hidden) {
        myHidden = hidden;
    }

    @Override
    public String toString() {
        return myName + " (" + myTrackCount + ")";
    }
}