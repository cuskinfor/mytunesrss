/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.Genre
 */
public class Genre implements Comparable<Genre> {
    private String myName;
    private int myAlbumCount;
    private int myTrackCount;
    private int myArtistCount;
    private boolean myHidden;

    public Genre() {
    }

    public Genre(String name, int albumCount, int trackCount, int artistCount, boolean hidden) {
        myName = name;
        myAlbumCount = albumCount;
        myTrackCount = trackCount;
        myArtistCount = artistCount;
        myHidden = hidden;
    }

    public int getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(int albumCount) {
        myAlbumCount = albumCount;
    }

    public int getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(int artistCount) {
        myArtistCount = artistCount;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }

    public boolean isHidden() {
        return myHidden;
    }

    public void setHidden(boolean hidden) {
        myHidden = hidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre)) return false;

        Genre genre = (Genre) o;

        if (!myName.equalsIgnoreCase(genre.myName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return myName.toLowerCase().hashCode();
    }

    public int compareTo(Genre o) {
        return myName.toLowerCase().compareTo(o.myName.toLowerCase());
    }
}
