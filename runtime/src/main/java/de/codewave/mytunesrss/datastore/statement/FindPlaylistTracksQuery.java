/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQuery
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {
    public static final String PSEUDO_ID_ALL_BY_ARTIST = "PlaylistAllByArtist";
    public static final String PSEUDO_ID_ALL_BY_ALBUM = "PlaylistAllByAlbum";

    private String myId;
    private SortOrder mySortOrder;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private ResultSetType myResultSetType = ResultSetType.TYPE_SCROLL_INSENSITIVE;
    private MediaType[] myMediaTypes;
    private VideoType myVideoType;
    private String[] myPermittedDataSources;
    
    public FindPlaylistTracksQuery(String id, SortOrder sortOrder) {
        myId = id;
        mySortOrder = sortOrder;
    }

    public FindPlaylistTracksQuery(User user, String id, SortOrder sortOrder) {
        this(id, sortOrder);
        myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        myMediaTypes = FindTrackQuery.getQueryMediaTypes(user);
        myVideoType = FindTrackQuery.getQueryVideoType(user);
        myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
    }

    public void setResultSetType(ResultSetType resultSetType) {
        myResultSetType = resultSetType;
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty() && (myRestrictedPlaylistIds.size() > 1 || !myRestrictedPlaylistIds.get(0).equals(myId)));
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("videotype", myVideoType != null);
        conditionals.put("datasource", myPermittedDataSources != null);
        if (PSEUDO_ID_ALL_BY_ALBUM.equals(myId) || PSEUDO_ID_ALL_BY_ARTIST.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracks", conditionals, myResultSetType);
            conditionals.put("albumorder", PSEUDO_ID_ALL_BY_ALBUM.equals(myId));
            conditionals.put("artistorder", PSEUDO_ID_ALL_BY_ARTIST.equals(myId));
        } else {
            conditionals.put("indexorder", mySortOrder != SortOrder.Album && mySortOrder != SortOrder.Artist);
            conditionals.put("albumorder", mySortOrder == SortOrder.Album);
            conditionals.put("artistorder", mySortOrder == SortOrder.Artist);
            String[] parts = StringUtils.split(myId, "@");
            if (parts.length == 3) {
                conditionals.put("index", parts.length == 3);
            }
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracks", conditionals, myResultSetType);
            statement.setString("id", parts[0]);
            if (parts.length == 3) {
                statement.setInt("firstIndex", Integer.parseInt(parts[1]));
                statement.setInt("lastIndex", Integer.parseInt(parts[2]));
            }
        }
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("datasources", myPermittedDataSources);
        FindTrackQuery.setQueryMediaAndVideoTypes(statement, myMediaTypes, myVideoType);
        return execute(statement, new TrackResultBuilder());
    }
}
