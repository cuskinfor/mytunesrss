/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistsQuery
 */
public class FindPlaylistsQuery extends DataStoreQuery<Playlist> {
    private PlaylistResultBuilder myBuilder = new PlaylistResultBuilder();

    public Collection<Playlist> execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT p.id AS id, p.name AS name, COUNT(ltp.track_id) AS track_count FROM playlist p, link_track_playlist ltp WHERE ltp.playlist_id = p.id GROUP BY p.id, p.name ORDER BY p.name");
        return execute(statement, myBuilder);
    }

    public static class PlaylistResultBuilder implements ResultBuilder<Playlist> {
        private PlaylistResultBuilder() {
            // intentionally left blank
        }

        public Playlist create(ResultSet resultSet) throws SQLException {
            Playlist playlist = new Playlist();
            playlist.setId(resultSet.getString("ID"));
            playlist.setName(resultSet.getString("NAME"));
            playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return playlist;
        }
    }}