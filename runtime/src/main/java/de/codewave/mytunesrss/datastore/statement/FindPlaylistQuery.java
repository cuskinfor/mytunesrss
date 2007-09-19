/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery
 */
public class FindPlaylistQuery extends DataStoreQuery<Collection<Playlist>> {
    private String myId;
    private PlaylistType myType;
    private String myRestrictionPlaylistId;

    public FindPlaylistQuery(PlaylistType type, String id) {
        myType = type;
        myId = id;
    }

    public FindPlaylistQuery(User user, PlaylistType type, String id) {
        this(type, id);
        myRestrictionPlaylistId = user.getPlaylistId();
    }

    public Collection<Playlist> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, StringUtils.isEmpty(myRestrictionPlaylistId) ? "findPlaylists" : "findPlaylistsRestricted");
        statement.setString("type", myType != null ? myType.name() : null);
        statement.setString("id", myId);
        statement.setString("restrictionPlaylistId", myRestrictionPlaylistId);
        return execute(statement, new PlaylistResultBuilder());
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