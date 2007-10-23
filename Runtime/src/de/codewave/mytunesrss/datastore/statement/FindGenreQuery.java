/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindGenreQuery extends DataStoreQuery<Collection<Genre>> {
    private int myIndex = -1;
    private FindGenreQuery.GenreResultBuilder myBuilder = new FindGenreQuery.GenreResultBuilder();

    public FindGenreQuery() {
        // intentionally left blank
    }

    public FindGenreQuery(int index) {
        myIndex = index;
    }

    public Collection<Genre> execute(Connection connection) throws SQLException {
        PreparedStatement statement;
        if (myIndex > -1) {
            statement = connection.prepareStatement(
                    "SELECT name, track_count, artist_count, album_count FROM genre WHERE " + PagerConfig.CONDITION[myIndex] + " ORDER BY name");
            return execute(statement, myBuilder);
        } else {
            statement = connection.prepareStatement("SELECT name, track_count, artist_count, album_count FROM genre ORDER BY name");
            return execute(statement, myBuilder);
        }
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