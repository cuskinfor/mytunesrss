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
    private String myUserName;
    private boolean myIncludeHidden;
    private boolean myMatchingOwnerOnly;

    public FindPlaylistQuery(PlaylistType type, String id, boolean includeHidden) {
        myType = type;
        myId = id;
        myIncludeHidden = includeHidden;
    }

    public FindPlaylistQuery(User user, PlaylistType type, String id, boolean includeHidden, boolean matchingOwnerOnly) {
        this(type, id, includeHidden);
        myRestrictionPlaylistId = user.getPlaylistId();
        myUserName = user.getName();
        myMatchingOwnerOnly = matchingOwnerOnly;
    }

    public Collection<Playlist> execute(Connection connection) throws SQLException {
        String name = myMatchingOwnerOnly ? "findUserPlaylists" : (StringUtils.isEmpty(myRestrictionPlaylistId) ? "findPlaylists" : "findPlaylistsRestricted");
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, name);
        statement.setString("type", myType != null ? myType.name() : null);
        statement.setString("id", myId);
        statement.setString("restrictionPlaylistId", myRestrictionPlaylistId);
        statement.setString("username", myUserName);
        statement.setBoolean("includeHidden", myIncludeHidden);
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
            playlist.setUserPrivate(resultSet.getBoolean("USER_PRIVATE"));
            return playlist;
        }
    }
}