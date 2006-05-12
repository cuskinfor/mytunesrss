/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.PlaylistResultBuilder
 */
public class PlaylistResultBuilder implements ResultBuilder<Playlist> {
    public Playlist create(ResultSet resultSet) throws SQLException {
        Playlist playlist = new Playlist();
        playlist.setId(resultSet.getString("ID"));
        playlist.setName(resultSet.getString("NAME"));
        playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
        return playlist;
    }
}