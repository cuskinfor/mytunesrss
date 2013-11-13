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
import org.apache.commons.lang.StringUtils;

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
    private String myGenre;
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
    public FindAlbumQuery(User user, String filter, String artist, boolean matchAlbumArtist, String genre, int index, int minYear, int maxYear, boolean sortByYear, boolean albumsBeforeCompilations, AlbumType type) {
        myFilter = StringUtils.isNotEmpty(filter) ? "%" + MyTunesRssUtils.toSqlLikeExpression(StringUtils.lowerCase(filter)) + "%" : null;
        myArtist = artist;
        myMatchAlbumArtist = matchAlbumArtist;
        myGenre = genre;
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
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("index", MyTunesRssUtils.isLetterPagerIndex(myIndex));
        conditionals.put("track", StringUtils.isNotBlank(myArtist) || StringUtils.isNotBlank(myGenre) || !myRestrictedPlaylistIds.isEmpty() || !myExcludedPlaylistIds.isEmpty());
        conditionals.put("filter", StringUtils.isNotBlank(myFilter));
        conditionals.put("artist", StringUtils.isNotBlank(myArtist) && !myMatchAlbumArtist);
        conditionals.put("albumartist", StringUtils.isNotBlank(myArtist) && myMatchAlbumArtist);
        conditionals.put("genre", StringUtils.isNotBlank(myGenre));
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
        conditionals.put("track", (myMediaTypes != null && myMediaTypes.length > 0) || myPermittedDataSources != null);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAlbums", conditionals);
        statement.setString("filter", myFilter);
        statement.setString("artist", StringUtils.lowerCase(myArtist));
        statement.setString("genre", StringUtils.lowerCase(myGenre));
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
