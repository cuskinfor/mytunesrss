/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery
 */
public class FindPlaylistQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Playlist>> {
    private String myId;
    private String myContainerId;
    private List<PlaylistType> myTypes;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private List<String> myHiddenPlaylistIds = Collections.emptyList();
    private String myUserName;
    private boolean myIncludeHidden;
    private boolean myMatchingOwnerOnly;

    public FindPlaylistQuery(List<PlaylistType> types, String id, String containerId, boolean includeHidden) {
        myTypes = types;
        myId = id;
        myContainerId = containerId;
        myIncludeHidden = includeHidden;
    }

    public FindPlaylistQuery(User user, List<PlaylistType> types, String id, String containerId, boolean includeHidden, boolean matchingOwnerOnly) {
        this(types, id, containerId, includeHidden);
        myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        myExcludedPlaylistIds = user.getEffectiveExcludedPlaylistIds();
        myHiddenPlaylistIds = user.getHiddenPlaylistIds();
        myUserName = user.getName();
        myMatchingOwnerOnly = matchingOwnerOnly;
    }

    public QueryResult<Playlist> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("container", StringUtils.isNotBlank(myContainerId) && !"ROOT".equals(myContainerId));
        conditionals.put("rootcontainer", StringUtils.equals(myContainerId, "ROOT"));
        conditionals.put("nohidden", !myIncludeHidden);
        conditionals.put("matching", myMatchingOwnerOnly);
        conditionals.put("user", !myMatchingOwnerOnly && StringUtils.isNotBlank(myUserName));
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("hidden", !myHiddenPlaylistIds.isEmpty());
        conditionals.put("restricted_or_excluded", !myRestrictedPlaylistIds.isEmpty() || !myExcludedPlaylistIds.isEmpty());
        conditionals.put("types", myTypes != null && !myTypes.isEmpty());
        conditionals.put("id", StringUtils.isNotBlank(myId));
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findPlaylists", conditionals);
        if (myTypes != null && !myTypes.isEmpty()) {
            List<String> typeNames = new ArrayList<String>(myTypes.size());
            for (PlaylistType type : myTypes) {
                typeNames.add(type.name());
            }
            statement.setItems("types", typeNames);
        }
        statement.setString("id", myId);
        statement.setString("containerId", myContainerId);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("hiddenPlaylistIds", myHiddenPlaylistIds);
        statement.setString("username", myUserName);
        statement.setBoolean("includeHidden", myIncludeHidden);
        return execute(statement, new PlaylistResultBuilder());
    }

    public static class PlaylistResultBuilder implements ResultBuilder<Playlist> {
        public PlaylistResultBuilder() {
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
            playlist.setContainerId(resultSet.getString("CONTAINER_ID"));
            return playlist;
        }
    }
}