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
    public static enum Operation {
        And(), Or();
    }

    private TrackResultBuilder myBuilder = new TrackResultBuilder();
    private String myQuery;
    private Object[] myParameters;

    public FindTrackQuery(String id) {
        myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE id = ?";
        myParameters = new String[] {id};
    }

    public FindTrackQuery(String searchTerm, Operation operation) {
        if (operation == Operation.And) {
            myQuery =
                    "SELECT id, name, artist, album, time, track_number, file FROM track WHERE name LIKE ? AND album LIKE ? AND artist LIKE ? ORDER BY album, track_number, name";
        } else {
            myQuery =
                    "SELECT id, name, artist, album, time, track_number, file FROM track WHERE name LIKE ? OR album LIKE ? OR artist LIKE ? ORDER BY album, track_number, name";
        }
        myParameters = new String[] {searchTerm, searchTerm, searchTerm};
    }

    public FindTrackQuery(String album, String artist) {
        if (StringUtils.isEmpty(album) && StringUtils.isEmpty(artist)) {
            throw new IllegalArgumentException("Either album or artist must be specified.");
        } else if (StringUtils.isNotEmpty(album) && StringUtils.isNotEmpty(artist)) {
            myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE album LIKE ? AND artist LIKE ? ORDER BY album, track_number, name";
            myParameters = new String[] {album, artist};
        } else if (StringUtils.isNotEmpty(album)) {
            myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE album LIKE ? ORDER BY track_number, name";
            myParameters = new String[] {album};
        } else {
            myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE artist LIKE ? ORDER BY album, track_number, name";
            myParameters = new String[] {artist};
        }
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