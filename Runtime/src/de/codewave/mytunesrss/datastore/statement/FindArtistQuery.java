/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindArtistQuery extends DataStoreQuery<Artist> {
    private String myAlbum;
    private ArtistResultBuilder myBuilder = new ArtistResultBuilder();

    public FindArtistQuery(String album) {
        myAlbum = album;
    }

    public Collection<Artist> execute(Connection connection) throws SQLException {
        PreparedStatement statement = null;
        if (myAlbum != null) {
            statement = connection.prepareStatement(
                "SELECT DISTINCT(t1.artist) AS artist, COUNT(DISTINCT(t2.album)) AS album_count, COUNT(DISTINCT(t2.id)) AS track_count FROM track t1, track t2 WHERE t1.album = ? AND t1.artist = t2.artist GROUP BY artist ORDER BY artist");
            return execute(statement, myBuilder, myAlbum);
        } else {
            statement = connection.prepareStatement(
                    "SELECT DISTINCT(t1.artist) AS artist, COUNT(DISTINCT(t2.album)) AS album_count, COUNT(DISTINCT(t2.id)) AS track_count FROM track t1, track t2 WHERE t1.artist = t2.artist GROUP BY artist ORDER BY artist");
            return execute(statement, myBuilder);
        }
    }

    public static class ArtistResultBuilder implements ResultBuilder<Artist> {

        private ArtistResultBuilder() {
            // intentionally left blank
        }

        public Artist create(ResultSet resultSet) throws SQLException {
            Artist artist = new Artist();
            artist.setName(resultSet.getString("ARTIST"));
            artist.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
            artist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return artist;
        }
    }
}