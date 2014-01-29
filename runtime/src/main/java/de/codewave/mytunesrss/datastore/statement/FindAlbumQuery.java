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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindAlbumQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Album>> {

    public enum AlbumType {
        COMPILATIONS(), ALBUMS(), ALL();
    }

    private String myFilter;
    private String myArtist;
    private String[] myGenres;
    private int myIndex;
    private int myMinYear;
    private int myMaxYear;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private AlbumType myType;
    private boolean mySortByYear;
    private boolean myAlbumsBeforeCompilations;
    private boolean myMatchAlbumArtist;
    private MediaType[] myMediaTypes;
    private String[] myPermittedDataSources;

    public FindAlbumQuery(User user, String filter, String artist, boolean matchAlbumArtist, String[] genres, int index, int minYear, int maxYear, boolean sortByYear, boolean albumsBeforeCompilations, AlbumType type) {
        myFilter = StringUtils.isNotEmpty(filter) ? "%" + MyTunesRssUtils.toSqlLikeExpression(StringUtils.lowerCase(filter)) + "%" : null;
        myArtist = artist;
        myMatchAlbumArtist = matchAlbumArtist;
        myGenres = genres;
        myIndex = index;
        myMinYear = minYear >= 0 ? minYear : Integer.MIN_VALUE;
        myMaxYear = (maxYear >= 0 && maxYear >= minYear) ? maxYear : Integer.MAX_VALUE;
        myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        mySortByYear = sortByYear;
        myAlbumsBeforeCompilations = albumsBeforeCompilations;
        myType = type;
        myMediaTypes = FindTrackQuery.getQueryMediaTypes(user);
        myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
    }

    public QueryResult<Album> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("index", MyTunesRssUtils.isLetterPagerIndex(myIndex));
        conditionals.put("filter", StringUtils.isNotBlank(myFilter));
        conditionals.put("artist", StringUtils.isNotBlank(myArtist) && !myMatchAlbumArtist);
        conditionals.put("albumartist", StringUtils.isNotBlank(myArtist) && myMatchAlbumArtist);
        conditionals.put("genre", myGenres != null && myGenres.length > 0);
        conditionals.put("year", myMinYear > Integer.MIN_VALUE || myMaxYear < Integer.MAX_VALUE);
        conditionals.put("albumorder", !mySortByYear && !myAlbumsBeforeCompilations);
        conditionals.put("compilationalbumorder", !mySortByYear && myAlbumsBeforeCompilations);
        conditionals.put("yearorder", mySortByYear && !myAlbumsBeforeCompilations);
        conditionals.put("compilationyearorder", mySortByYear && myAlbumsBeforeCompilations);
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("compilation", myType != AlbumType.ALL);
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("datasource", myPermittedDataSources != null);
        conditionals.put("track", (myMediaTypes != null && myMediaTypes.length > 0) || myPermittedDataSources != null || StringUtils.isNotBlank(myArtist) || (myGenres != null && myGenres.length > 0) || !myRestrictedPlaylistIds.isEmpty() || !myExcludedPlaylistIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAlbums", conditionals);
        statement.setString("filter", myFilter);
        statement.setString("artist", StringUtils.lowerCase(myArtist));
        statement.setItems("genre", MyTunesRssUtils.toLowerCase(myGenres));
        statement.setInt("index", myIndex);
        statement.setInt("min_year", myMinYear);
        statement.setInt("max_year", myMaxYear);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setInt("compilation", myType == AlbumType.COMPILATIONS ? 1 : 0);
        statement.setItems("datasources", myPermittedDataSources);
        FindTrackQuery.setQueryMediaTypes(statement, myMediaTypes);
        return execute(statement, new AlbumResultBuilder());
    }

    public static class AlbumResultBuilder implements ResultBuilder<Album> {
        private AlbumResultBuilder() {
            // intentionally left blank
        }

        public Album create(ResultSet resultSet) throws SQLException {
            Album album = new Album();
            album.setName(resultSet.getString("ALBUMNAME"));
            album.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            album.setArtistCount(resultSet.getInt("ARTIST_COUNT"));
            album.setArtist(resultSet.getString("ARTIST"));
            album.setImageHash(StringUtils.trimToNull(resultSet.getString("IMAGE_HASH")));
            album.setYear(resultSet.getInt("YEAR"));
            return album;
        }
    }
}
