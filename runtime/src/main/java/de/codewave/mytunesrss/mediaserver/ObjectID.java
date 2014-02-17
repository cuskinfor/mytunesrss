/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

public enum ObjectID {

    Playlists("ps"), Albums("as"), Album("a"), AlbumTrack("at"), Artists("ars"), ArtistAlbums("aras"), ArtistAlbum("ara"), ArtistAlbumTrack("aat"), Genres("gs"), GenreAlbums("gas"), GenreAlbum("ga"), GenreAlbumTrack("gat"), Movies("ms"), Movie("m"), TvShows("vs"), TvShow("v"), TvShowSeason("vv"), TvShowEpisode("vvv"), Photoalbums("pas");

    public static ObjectID fromValue(String value) {
        for (ObjectID objectID : ObjectID.values()) {
            if (objectID.getValue().equals(value)) {
                return objectID;
            }
        }
        return null;
    }

    private String myValue;

    ObjectID(String value) {
        myValue = value;
    }

    public String getValue() {
        return myValue;
    }
}
