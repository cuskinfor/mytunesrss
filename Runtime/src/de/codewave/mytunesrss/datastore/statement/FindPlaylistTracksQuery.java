/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.lang.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<Track> {
    private FindPlaylistTracksQuery.TrackResultBuilder myBuilder = new FindPlaylistTracksQuery.TrackResultBuilder();
    private String myId;
    private int myFirst = -1;
    private int myLast = -1;

    public FindPlaylistTracksQuery(String id) {
        String[] parts = StringUtils.split(id, "_");
        myId = parts[0];
        if (parts.length == 3) {
            myFirst = Integer.parseInt(parts[1]);
            myLast = Integer.parseInt(parts[2]);
        }
    }

    public Collection<Track> execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT ltp.index AS index, t.id AS id, t.name AS name, t.artist AS artist, t.album AS album, t.time AS time, t.track_number AS track_number, t.file AS file FROM track t, link_track_playlist ltp WHERE t.id = ltp.track_id AND ltp.playlist_id = ? ORDER BY index");
        List<Track> tracks = (List<Track>)execute(statement, myBuilder, myId);
        return myFirst == -1 ? tracks : tracks.subList(myFirst, myLast + 1);
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