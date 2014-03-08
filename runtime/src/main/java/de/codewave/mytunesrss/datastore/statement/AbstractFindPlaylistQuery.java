/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery
 */
public abstract class AbstractFindPlaylistQuery<T> extends DataStoreQuery<T> {
    private String myId;
    private String myContainerId;
    private List<PlaylistType> myTypes;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private List<String> myHiddenPlaylistIds = Collections.emptyList();
    private String myUserName;
    private boolean myIncludeHidden;
    private boolean myMatchingOwnerOnly;
    private MediaType[] myMediaTypes;
    private String[] myPermittedDataSources;

    protected AbstractFindPlaylistQuery(List<PlaylistType> types, String id, String containerId, boolean includeHidden) {
        myTypes = types;
        myId = id;
        myContainerId = containerId;
        myIncludeHidden = includeHidden;
    }

    protected AbstractFindPlaylistQuery(User user, List<PlaylistType> types, String id, String containerId, boolean includeHidden, boolean matchingOwnerOnly) {
        this(types, id, containerId, includeHidden);
        myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        myHiddenPlaylistIds = user.getHiddenPlaylistIds();
        myUserName = user.getName();
        myMatchingOwnerOnly = matchingOwnerOnly;
        myMediaTypes = FindTrackQuery.getQueryMediaTypes(user);
        myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
    }

    protected SmartStatement createStatement(Connection connection, Map<String, Boolean> conditionals) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findPlaylists", conditionals);
        if (myTypes != null && !myTypes.isEmpty()) {
            List<String> typeNames = new ArrayList<>(myTypes.size());
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
        statement.setItems("datasources", myPermittedDataSources);
        FindTrackQuery.setQueryMediaTypes(statement, myMediaTypes);
        return statement;
    }

    protected Map<String, Boolean> getConditionals() {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("container", StringUtils.isNotBlank(myContainerId) && !"ROOT".equals(myContainerId));
        conditionals.put("rootcontainer", StringUtils.equals(myContainerId, "ROOT"));
        conditionals.put("nohidden", !myIncludeHidden);
        conditionals.put("matching", myMatchingOwnerOnly);
        conditionals.put("user", !myMatchingOwnerOnly && StringUtils.isNotBlank(myUserName));
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("hidden", !myHiddenPlaylistIds.isEmpty());
        boolean track = (myMediaTypes != null && myMediaTypes.length > 0) || myPermittedDataSources != null;
        conditionals.put("track_or_restricted_or_excluded", !myRestrictedPlaylistIds.isEmpty() || !myExcludedPlaylistIds.isEmpty() || track);
        conditionals.put("types", myTypes != null && !myTypes.isEmpty());
        conditionals.put("id", StringUtils.isNotBlank(myId));
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("datasource", myPermittedDataSources != null);
        conditionals.put("track", track);
        return conditionals;
    }

}
