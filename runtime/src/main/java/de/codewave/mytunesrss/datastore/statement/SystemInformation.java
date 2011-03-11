/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SystemInformation
 */
public class SystemInformation {
    private long myLastUpdate = Long.MIN_VALUE;
    private String myVersion;
    private int myMusicCount;
    private int myMovieCount;
    private int myTvShowCount;
    private int myTrackCount;
    private int myAlbumCount;
    private int myArtistCount;
    private int myGenreCount;
    private int myPhotoCount;

    public long getLastUpdate() {
        return myLastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        myLastUpdate = lastUpdate;
    }

    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
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

    public int getGenreCount() {
        return myGenreCount;
    }

    public void setGenreCount(int genreCount) {
        myGenreCount = genreCount;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }

    public int getMusicCount() {
        return myMusicCount;
    }

    public void setMusicCount(int musicCount) {
        myMusicCount = musicCount;
    }

    public int getMovieCount() {
        return myMovieCount;
    }

    public void setMovieCount(int movieCount) {
        myMovieCount = movieCount;
    }

    public int getTvShowCount() {
        return myTvShowCount;
    }

    public void setTvShowCount(int tvShowCount) {
        myTvShowCount = tvShowCount;
    }

    public int getPhotoCount() {
        return myPhotoCount;
    }

    public void setPhotoCount(int photoCount) {
        myPhotoCount = photoCount;
    }

    public boolean isAnyContent() {
        return myTrackCount + myAlbumCount + myArtistCount + myGenreCount + myMovieCount + myTvShowCount + myPhotoCount > 0;
    }
}