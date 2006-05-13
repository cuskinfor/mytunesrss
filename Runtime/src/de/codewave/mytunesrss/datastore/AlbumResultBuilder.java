/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.AlbumResultBuilder
 */
public class AlbumResultBuilder implements ResultBuilder<Album>{
    public Album create(ResultSet resultSet) throws SQLException {
        Album album = new Album();
        album.setName(resultSet.getString("ALBUM"));
        album.setArtistCount(resultSet.getInt("ARTIST_COUNT"));
        album.setTrackCount(resultSet.getInt("TRACK_COUNT"));
        return album;
    }
}