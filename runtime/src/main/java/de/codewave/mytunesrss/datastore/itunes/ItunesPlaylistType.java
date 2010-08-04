/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.itunes;

public enum ItunesPlaylistType {
    PartyShuffle("Party Shuffle"), Podcasts("Podcasts"), Audiobooks("Audiobooks"), Movies("Movies"), Music("Music"), TvShows("TV Shows"),
    PurchasedMusic("Purchased Music"), Books("Books"), SmartPlaylists("Smart Playlists"), Master("Master");

    private final String myText;

    ItunesPlaylistType(String text) {
        myText = text;
    }

    public String toString() {
        return myText;
    }
}
