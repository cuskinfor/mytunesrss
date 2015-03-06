/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery
 */
public class FindPlaylistQuery extends AbstractFindPlaylistQuery<QueryResult<Playlist>> {

    public static FindPlaylistQuery createForPlaylistManager(User user) {
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(Arrays.asList(PlaylistType.MyTunes, PlaylistType.MyTunesSmart), null, null, false);
        findPlaylistQuery.setMatchingOwnerOnly(user);
        return findPlaylistQuery;
    }

    public FindPlaylistQuery(List<PlaylistType> types, String id, String containerId, boolean includeHidden) {
        super(types, id, containerId, includeHidden);
    }

    public FindPlaylistQuery(User user, List<PlaylistType> types, String id, String containerId, boolean includeHidden, boolean matchingOwnerOnly) {
        super(user, types, id, containerId, includeHidden, matchingOwnerOnly);
    }

    @Override
    public QueryResult<Playlist> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = getConditionals();
        conditionals.put("selectAll", true);
        conditionals.put("selectId", false);
        SmartStatement statement = createStatement(connection, conditionals);
        return execute(statement, new PlaylistResultBuilder());
    }

    public static class PlaylistResultBuilder implements ResultBuilder<Playlist> {

        @Override
        public Playlist create(ResultSet resultSet) throws SQLException {
            Playlist playlist = new Playlist();
            playlist.setId(resultSet.getString("ID"));
            playlist.setName(resultSet.getString("NAME"));
            playlist.setType(PlaylistType.valueOf(resultSet.getString("TYPE")));
            playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            playlist.setUserPrivate(resultSet.getBoolean("USER_PRIVATE"));
            playlist.setHidden(resultSet.getBoolean("HIDDEN"));
            playlist.setUserOwner(resultSet.getString("USER_OWNER"));
            playlist.setContainerId(resultSet.getString("CONTAINER_ID"));
            return playlist;
        }
    }
}
