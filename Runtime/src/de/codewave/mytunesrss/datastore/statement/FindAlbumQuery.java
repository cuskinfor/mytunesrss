/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.lang.*;

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
        if (StringUtils.isNotEmpty(myArtist)) {
            statement = connection.prepareStatement("SELECT name, track_count, artist_count, artist FROM album WHERE name IN ( SELECT DISTINCT(album) FROM track WHERE artist = ? ) ORDER BY name");
            return execute(statement, myBuilder, myArtist);
        } else {
            statement = connection.prepareStatement("SELECT name, track_count, artist_count, artist FROM album ORDER BY name");
            return execute(statement, myBuilder);
        }
    }

    public static class AlbumResultBuilder implements ResultBuilder<Album> {
        private AlbumResultBuilder() {
            // intentionally left blank
        }

        public Album create(ResultSet resultSet) throws SQLException {
            Album album = new Album();
            album.setName(resultSet.getString("NAME"));
            album.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            album.setArtistCount(resultSet.getInt("ARTIST_COUNT"));
            album.setArtist(resultSet.getString("ARTIST"));
            return album;
        }
    }
}