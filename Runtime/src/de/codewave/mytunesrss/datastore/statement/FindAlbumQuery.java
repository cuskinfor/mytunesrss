/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindAlbumQuery extends DataStoreQuery<Album> {
    private String myArtist;
    private AlbumResultBuilder myBuilder = new AlbumResultBuilder();

    public FindAlbumQuery(String artist) {
        myArtist = artist;
    }

    public Collection<Album> execute(Connection connection) throws SQLException {
        PreparedStatement statement;
        if (myArtist != null) {
        statement = connection.prepareStatement(
                "SELECT DISTINCT(t1.album) AS album, COUNT(DISTINCT(t2.artist)) AS artist_count, COUNT(DISTINCT(t2.id)) AS track_count FROM track t1, track t2 WHERE t1.artist = ? AND t1.album = t2.album GROUP BY album ORDER BY album");
            return execute(statement, myBuilder, myArtist);
        } else {
            statement = connection.prepareStatement(
                    "SELECT DISTINCT(t1.album) AS album, COUNT(DISTINCT(t2.artist)) AS artist_count, COUNT(DISTINCT(t2.id)) AS track_count FROM track t1, track t2 WHERE t1.album = t2.album GROUP BY album ORDER BY album");
            return execute(statement, myBuilder);
        }
    }

    public static class AlbumResultBuilder implements ResultBuilder<Album> {
        private AlbumResultBuilder() {
            // intentionally left blank
        }

        public Album create(ResultSet resultSet) throws SQLException {
            Album album = new Album();
            album.setName(resultSet.getString("ALBUM"));
            album.setArtistCount(resultSet.getInt("ARTIST_COUNT"));
            album.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return album;
        }
    }
}