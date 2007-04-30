/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindArtistQuery extends DataStoreQuery<Collection<Artist>> {
    public static FindArtistQuery getForAlbum(String album) {
        FindArtistQuery query = new FindArtistQuery();
        query.myAlbum = album;
        return query;
    }

    public static FindArtistQuery getForGenre(String genre) {
        FindArtistQuery query = new FindArtistQuery();
        query.myGenre = genre;
        return query;
    }

    public static FindArtistQuery getForPagerIndex(int index) {
        FindArtistQuery query = new FindArtistQuery();
        query.myIndex = index;
        return query;
    }

    public static FindArtistQuery getAll() {
        return new FindArtistQuery();
    }

    private String myAlbum;
    private String myGenre;
    private int myIndex = -1;
    private ArtistResultBuilder myBuilder = new ArtistResultBuilder();

    private FindArtistQuery() {
        // intentionally left blank
    }

    public Collection<Artist> execute(Connection connection) throws SQLException {
        PreparedStatement statement = null;
        if (StringUtils.isNotEmpty(myAlbum)) {
            statement = connection.prepareStatement(
                    "SELECT name, track_count, album_count FROM artist WHERE name IN ( SELECT DISTINCT(artist) FROM track WHERE album = ? ) ORDER BY name");
            return execute(statement, myBuilder, myAlbum);
        } else if (StringUtils.isNotEmpty(myGenre)) {
            statement = connection.prepareStatement(
                    "SELECT name, track_count, album_count FROM artist WHERE name IN ( SELECT DISTINCT(artist) FROM track WHERE genre = ? ) ORDER BY name");
            return execute(statement, myBuilder, myGenre);
        } else if (myIndex > -1) {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT condition AS condition FROM pager WHERE type = '" + InsertPageStatement.PagerType.Artist + "' AND index = " + myIndex);
            resultSet.next();
            statement = connection.prepareStatement(
                    "SELECT name, track_count, album_count FROM artist WHERE " + resultSet.getString("CONDITION") + " ORDER BY name");
            return execute(statement, myBuilder);
        } else {
            statement = connection.prepareStatement("SELECT name, track_count, album_count FROM artist ORDER BY name");
            return execute(statement, myBuilder);
        }
    }

    public static class ArtistResultBuilder implements ResultBuilder<Artist> {

        private ArtistResultBuilder() {
            // intentionally left blank
        }

        public Artist create(ResultSet resultSet) throws SQLException {
            Artist artist = new Artist();
            artist.setName(resultSet.getString("NAME"));
            artist.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
            artist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return artist;
        }
    }
}