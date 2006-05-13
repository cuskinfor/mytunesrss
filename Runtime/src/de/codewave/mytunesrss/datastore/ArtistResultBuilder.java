/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.ArtistResultBuilder
 */
public class ArtistResultBuilder implements ResultBuilder<Artist> {
    public Artist create(ResultSet resultSet) throws SQLException {
        Artist artist = new Artist();
        artist.setName(resultSet.getString("ARTIST"));
        artist.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
        artist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
        return artist;
    }
}