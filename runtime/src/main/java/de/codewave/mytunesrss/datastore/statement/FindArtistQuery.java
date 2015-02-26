/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;

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
public class FindArtistQuery extends MyTunesRssDataStoreQuery<QueryResult<Artist>> {
    private String myFilter;
    private String myAlbum;
    private String[] myGenres;
    private int myIndex;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private MediaType[] myMediaTypes;
    private String[] myPermittedDataSources;

    public FindArtistQuery(User user, String filter, String album, String[] genres, int index) {
        myFilter = StringUtils.isNotEmpty(filter) ? "%" + MyTunesRssUtils.toSqlLikeExpression(StringUtils.lowerCase(filter)) + "%" : null;
        myAlbum = album;
        myGenres = genres != null ? genres.clone() : null;
        myIndex = index;
        myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        myMediaTypes = FindTrackQuery.getQueryMediaTypes(user);
        myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
        setForceEmptyResult(!user.isAudio());
    }

    public QueryResult<Artist> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("index", MyTunesRssUtils.isLetterPagerIndex(myIndex));
        conditionals.put("filter", StringUtils.isNotBlank(myFilter));
        conditionals.put("artist", StringUtils.isNotBlank(myAlbum));
        conditionals.put("genre", myGenres != null && myGenres.length > 0);
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("datasource", myPermittedDataSources != null);
        conditionals.put("track", (myMediaTypes != null && myMediaTypes.length > 0) || myPermittedDataSources != null || StringUtils.isNotBlank(myAlbum) || (myGenres != null && myGenres.length > 0) || !myRestrictedPlaylistIds.isEmpty() || !myExcludedPlaylistIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findArtists", conditionals);
        statement.setString("filter", myFilter);
        statement.setString("album", StringUtils.lowerCase(myAlbum));
        statement.setItems("genre", MyTunesRssUtils.toLowerCase(myGenres));
        statement.setInt("index", myIndex);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("datasources", myPermittedDataSources);
        FindTrackQuery.setQueryMediaTypes(statement, myMediaTypes);
        return execute(statement, new ArtistResultBuilder());
    }

    public static class ArtistResultBuilder implements ResultBuilder<Artist> {
        private ArtistResultBuilder() {
            // intentionally left blank
        }

        public Artist create(ResultSet resultSet) throws SQLException {
            Artist artist = new Artist();
            artist.setName(resultSet.getString("NAME"));
            artist.setNaturalSortName(resultSet.getString("NAT_SORT_NAME"));
            artist.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
            artist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return artist;
        }
    }
}
