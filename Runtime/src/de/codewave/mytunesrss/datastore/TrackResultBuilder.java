/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.sql.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.datastore.TrackResultBuilder
 */
public class TrackResultBuilder implements ResultBuilder<Track> {
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