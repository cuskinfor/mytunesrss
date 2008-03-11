/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
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
public class FindGenreQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Genre>> {
    private int myIndex;
    private String myRestrictedPlaylistId;

    public FindGenreQuery(User user, int index) {
       myIndex = index;
        myRestrictedPlaylistId = user.getPlaylistId();
    }

    public QueryResult<Genre> execute(Connection connection) throws SQLException {
      SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findGenres" + (StringUtils.isEmpty(myRestrictedPlaylistId) ? "" : "Restricted"));
      statement.setInt("index", myIndex);
      statement.setString("restrictedPlaylistId", myRestrictedPlaylistId);
      return execute(statement, new GenreResultBuilder());
    }

    public static class GenreResultBuilder implements ResultBuilder<Genre> {
        private GenreResultBuilder() {
            // intentionally left blank
        }

        public Genre create(ResultSet resultSet) throws SQLException {
            Genre genre = new Genre();
            genre.setName(resultSet.getString("NAME"));
            genre.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            genre.setArtistCount(resultSet.getInt("ARTIST_COUNT"));
            genre.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
            return genre;
        }
    }
}