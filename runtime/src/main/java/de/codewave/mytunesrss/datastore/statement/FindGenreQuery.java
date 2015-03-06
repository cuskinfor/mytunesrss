/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindGenreQuery extends MyTunesRssDataStoreQuery<Genre> {
    private String myName;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private MediaType[] myMediaTypes;
    private String[] myPermittedDataSources;

    public FindGenreQuery(User user, String name) {
        myName = name;
        if (user != null) {
            myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
            myExcludedPlaylistIds = user.getExcludedPlaylistIds();
            myMediaTypes = FindTrackQuery.getQueryMediaTypes(user);
            myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
            setForceEmptyResult(!user.isAudio());
        }
    }

    @Override
    public Genre execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("track", !myRestrictedPlaylistIds.isEmpty() || !myExcludedPlaylistIds.isEmpty());
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("datasource", myPermittedDataSources != null);
        conditionals.put("track", (myMediaTypes != null && myMediaTypes.length > 0) || myPermittedDataSources != null || !myRestrictedPlaylistIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findGenre", conditionals);
        statement.setString("name", myName);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("datasources", myPermittedDataSources);
        FindTrackQuery.setQueryMediaTypes(statement, myMediaTypes);
        setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1);
        QueryResult<Genre> genres = execute(statement, new GenreResultBuilder());
        return genres.nextResult();
    }

    public static class GenreResultBuilder implements ResultBuilder<Genre> {
        private GenreResultBuilder() {
            // intentionally left blank
        }

        @Override
        public Genre create(ResultSet resultSet) throws SQLException {
            Genre genre = new Genre();
            genre.setName(resultSet.getString("NAME"));
            genre.setNaturalSortName(resultSet.getString("NAT_SORT_NAME"));
            genre.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            genre.setArtistCount(resultSet.getInt("ARTIST_COUNT"));
            genre.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
            genre.setHidden(resultSet.getBoolean("HIDDEN"));
            return genre;
        }
    }
}
