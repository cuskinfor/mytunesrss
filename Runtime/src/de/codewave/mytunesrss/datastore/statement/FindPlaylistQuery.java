/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery
 */
public class FindPlaylistQuery extends DataStoreQuery<Playlist> {
    private PlaylistResultBuilder myBuilder = new PlaylistResultBuilder();
    private Object[] myParameters;
    private String mySql;

    public FindPlaylistQuery() {
        mySql = "SELECT id AS id, name AS name, type AS type, track_count AS track_count FROM playlist ORDER BY name";
    }

    public FindPlaylistQuery(PlaylistType type) {
        mySql = "SELECT id AS id, name AS name, type AS type, track_count AS track_count FROM playlist WHERE type = ? ORDER BY name";
        myParameters = new Object[]{type.name()};
    }

    public FindPlaylistQuery(String playlistId) {
        mySql = "SELECT id AS id, name AS name, type AS type, track_count AS track_count FROM playlist WHERE id = ?";
        myParameters = new Object[]{playlistId};
    }

    public Collection<Playlist> execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(mySql);
        return execute(statement, myBuilder, myParameters);
    }

    public static class PlaylistResultBuilder implements ResultBuilder<Playlist> {
        private PlaylistResultBuilder() {
            // intentionally left blank
        }

        public Playlist create(ResultSet resultSet) throws SQLException {
            Playlist playlist = new Playlist();
            playlist.setId(resultSet.getString("ID"));
            playlist.setName(resultSet.getString("NAME"));
            playlist.setType(PlaylistType.valueOf(resultSet.getString("TYPE")));
            playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return playlist;
        }
    }
}