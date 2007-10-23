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
public class FindAlbumQuery extends DataStoreQuery<Collection<Album>> {
    public static FindAlbumQuery getForGenre(String genre) {
        FindAlbumQuery query = new FindAlbumQuery();
        query.myGenre = genre;
        return query;
    }

    public static FindAlbumQuery getForArtist(String artist) {
        FindAlbumQuery query = new FindAlbumQuery();
        query.myArtist = artist;
        return query;
    }

    public static FindAlbumQuery getForPagerIndex(int index) {
        FindAlbumQuery query = new FindAlbumQuery();
        query.myIndex = index;
        return query;
    }

    private String myArtist;
    private String myGenre;
    private int myIndex = -1;
    private AlbumResultBuilder myBuilder = new AlbumResultBuilder();

    private FindAlbumQuery() {
        // intentionally left blank
    }

    public Collection<Album> execute(Connection connection) throws SQLException {
        PreparedStatement statement;
        if (StringUtils.isNotEmpty(myArtist)) {
            statement = connection.prepareStatement(
                    "SELECT name, track_count, artist_count, artist FROM album WHERE name IN ( SELECT DISTINCT(album) FROM track WHERE artist = ? ) ORDER BY name");
            /** same statement without help table (album)
             SELECT t1.album, count(distinct(t2.id)), count(distinct(t2.artist)), CASEWHEN(count(distinct(t2.artist)) = 1, GROUP_CONCAT(distinct(t2.artist)), '!')
             FROM track t1
             INNER JOIN track t2 ON t1.album = t2.album
             WHERE t1.artist = ?
             GROUP BY t1.album
             ORDER BY t1.album
             */
            return execute(statement, myBuilder, myArtist);
        } else if (StringUtils.isNotEmpty(myGenre)) {
            statement = connection.prepareStatement(
                    "SELECT name, track_count, artist_count, artist FROM album WHERE name IN ( SELECT DISTINCT(album) FROM track WHERE genre = ? ) ORDER BY name");
            /** same statement without help table (album)
             SELECT t1.album, count(distinct(t2.id)), count(distinct(t2.artist)), CASEWHEN(count(distinct(t2.artist)) = 1, GROUP_CONCAT(distinct(t2.artist)), '!')
             FROM track t1
             INNER JOIN track t2 ON t1.album = t2.album
             WHERE t1.genre = ?
             GROUP BY t1.album
             ORDER BY t1.album
             */
            return execute(statement, myBuilder, myGenre);
        } else if (myIndex > -1) {
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT condition AS condition FROM pager WHERE type = '" + InsertPageStatement.PagerType.Album + "' AND index = " + myIndex);
            resultSet.next();
            statement = connection.prepareStatement("SELECT name, track_count, artist_count, artist FROM album WHERE " + resultSet.getString(
                    "CONDITION") + " ORDER BY name");
            /**
             SELECT t1.album, count(distinct(t2.id)), count(distinct(t2.artist)), CASEWHEN(count(distinct(t2.artist)) = 1, GROUP_CONCAT(distinct(t2.artist)), '!')
             FROM track t1
             INNER JOIN track t2 ON t1.album = t2.album
             WHERE lower(substring(ltrim(t1.album), 0, 1)) >= ? AND lower(substring(ltrim(t1.album), 0, 1)) <= ?
             GROUP BY t1.album
             ORDER BY t1.album
             */
            return execute(statement, myBuilder);
        } else {
            statement = connection.prepareStatement("SELECT name, track_count, artist_count, artist FROM album ORDER BY name");
            /**
             SELECT t1.album, count(distinct(t2.id)), count(distinct(t2.artist)), CASEWHEN(count(distinct(t2.artist)) = 1, GROUP_CONCAT(distinct(t2.artist)), '!')
             FROM track t1
             INNER JOIN track t2 ON t1.album = t2.album
             GROUP BY t1.album
             ORDER BY t1.album
             */
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