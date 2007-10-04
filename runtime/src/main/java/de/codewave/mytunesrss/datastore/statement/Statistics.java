package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.Statistics
 */
public class Statistics {
    private int myTrackCount;
    private int myAlbumCount;
    private int myArtistCount;
    private int myGenreCount;

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
}