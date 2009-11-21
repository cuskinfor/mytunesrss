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
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindAlbumQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Album>> {

    private String myFilter;
    private String myArtist;
    private String myGenre;
    private int myIndex;
    private int myMinYear;
    private int myMaxYear;
    private String myRestrictedPlaylistId;
    private boolean mySortByYear;

    public FindAlbumQuery(User user, String filter, String artist, String genre, int index, int minYear, int maxYear, boolean sortByYear) {
        myFilter = StringUtils.isNotEmpty(filter) ? "%" + filter + "%" : null;
        myArtist = artist;
        myGenre = genre;
        myIndex = index;
        myMinYear = minYear >= 0 ? minYear : Integer.MIN_VALUE;
        myMaxYear = (maxYear >= 0 && maxYear >= minYear) ? maxYear : Integer.MAX_VALUE;
        myRestrictedPlaylistId = user.getPlaylistId();
        mySortByYear = sortByYear;
    }

    public QueryResult<Album> execute(Connection connection) throws SQLException {
        String statementName = "findAlbums";
        if (StringUtils.isNotEmpty(myRestrictedPlaylistId)) {
            statementName += "Restricted";
        }
        if (mySortByYear) {
            statementName += "SortByYear";
        }
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, statementName);
        statement.setString("filter", myFilter);
        statement.setString("artist", myArtist);
        statement.setString("genre", myGenre);
        statement.setInt("index", myIndex);
        statement.setInt("min_year", myMinYear);
        statement.setInt("max_year", myMaxYear);
        statement.setString("restrictedPlaylistId", myRestrictedPlaylistId);
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