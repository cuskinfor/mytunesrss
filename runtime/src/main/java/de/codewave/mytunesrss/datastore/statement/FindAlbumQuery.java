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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindAlbumQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Album>> {

    private String myFilter;
    private String myArtist;
    private String myGenre;
    private int myIndex;
    private int myMinYear;
    private int myMaxYear;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    ;
    private boolean mySortByYear;

    public FindAlbumQuery(User user, String filter, String artist, String genre, int index, int minYear, int maxYear, boolean sortByYear) {
        myFilter = StringUtils.isNotEmpty(filter) ? "%" + filter + "%" : null;
        myArtist = artist;
        myGenre = genre;
        myIndex = index;
        myMinYear = minYear >= 0 ? minYear : Integer.MIN_VALUE;
        myMaxYear = (maxYear >= 0 && maxYear >= minYear) ? maxYear : Integer.MAX_VALUE;
        myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        mySortByYear = sortByYear;
    }

    public QueryResult<Album> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("index", MyTunesRssUtils.isLetterPagerIndex(myIndex));
        conditionals.put("track", StringUtils.isNotBlank(myArtist) || StringUtils.isNotBlank(myGenre) || !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("filter", StringUtils.isNotBlank(myFilter));
        conditionals.put("artist", StringUtils.isNotBlank(myArtist));
        conditionals.put("genre", StringUtils.isNotBlank(myGenre));
        conditionals.put("year", myMinYear > Integer.MIN_VALUE || myMaxYear < Integer.MAX_VALUE);
        conditionals.put("albumorder", !mySortByYear);
        conditionals.put("yearorder", mySortByYear);
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAlbums", conditionals);
        statement.setString("filter", StringUtils.lowerCase(myFilter));
        statement.setString("artist", StringUtils.lowerCase(myArtist));
        statement.setString("genre", StringUtils.lowerCase(myGenre));
        statement.setInt("index", myIndex);
        statement.setInt("min_year", myMinYear);
        statement.setInt("max_year", myMaxYear);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
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
            album.setImageHash(resultSet.getString("IMAGE_HASH"));
            album.setYear(resultSet.getInt("YEAR"));
            return album;
        }
    }
}