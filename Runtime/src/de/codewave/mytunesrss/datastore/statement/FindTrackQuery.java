/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.lang.*;

import java.sql.*;
import java.util.*;
import java.io.*;

/**
 de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindTrackQuery extends DataStoreQuery<Track> {
    public static FindTrackQuery getForId(String id) {
        FindTrackQuery query = new FindTrackQuery();
        query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE id = ?";
        query.myParameters = new String[] {id};
        return query;
    }

    public static FindTrackQuery getForSearchTerm(String searchTerm, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        String artistSort = sortByArtistFirst ? "artist, " : "";
        query.myQuery =
                "SELECT id, name, artist, album, time, track_number, file FROM track WHERE name LIKE ? OR album LIKE ? OR artist LIKE ? ORDER BY " +
                        artistSort + "album, track_number, name";
        query.myParameters = new String[] {searchTerm, searchTerm, searchTerm};
        return query;
    }

    public static FindTrackQuery getForAlbum(String album, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        String artistSort = sortByArtistFirst ? "artist, " : "";
        query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE album = ? ORDER BY " + artistSort + "track_number, name";
        query.myParameters = new String[] {album};
        return query;
    }

    public static FindTrackQuery getForArtist(String artist, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        String artistSort = sortByArtistFirst ? "artist, " : "";
        query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE artist = ? ORDER BY " + artistSort + "album, track_number, name";
        query.myParameters = new String[] {artist};
        return query;
    }

    private TrackResultBuilder myBuilder = new TrackResultBuilder();
    private String myQuery;
    private Object[] myParameters;

    private FindTrackQuery() {
        // intentionally left blank
    }

    public Collection<Track> execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(myQuery);
        return execute(statement, myBuilder, myParameters);
    }

    public static class TrackResultBuilder implements ResultBuilder<Track> {
        private TrackResultBuilder() {
            // intentionally left blank
        }

        public Track create(ResultSet resultSet) throws SQLException {
            Track track = new Track();
            track.setId(resultSet.getString("ID"));
            track.setName(resultSet.getString("NAME"));
            track.setArtist(resultSet.getString("ARTIST"));
            track.setAlbum(resultSet.getString("ALBUM"));
            track.setTime(resultSet.getInt("TIME"));
            track.setTrackNumber(resultSet.getInt("TRACK_NUMBER"));
            track.setFile(new File(resultSet.getString("FILE")));
            return track;
        }
    }
}