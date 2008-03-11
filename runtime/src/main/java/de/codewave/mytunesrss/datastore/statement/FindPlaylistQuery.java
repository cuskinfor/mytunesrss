/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery
 */
public class FindPlaylistQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Playlist>> {
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

    public QueryResult<Playlist> execute(Connection connection) throws SQLException {
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
            playlist.setHidden(resultSet.getBoolean("HIDDEN"));
            playlist.setUserOwner(resultSet.getString("USER_OWNER"));
            return playlist;
        }
    }
}