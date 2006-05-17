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
public class FindTrackQuery extends DataStoreQuery<Track> {
    public static FindTrackQuery getForId(String[] trackIds) {
        FindTrackQuery query = new FindTrackQuery();
        if (trackIds.length > 1) {
            query.myQuery =
                    "SELECT id, name, artist, album, time, track_number, file FROM track WHERE id IN (" + SQLUtils.createParameters(
                            trackIds.length) + ")";
        } else {
            query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE id = ?";
        }
        query.myParameters = trackIds;
        return query;
    }


    public static FindTrackQuery getForSearchTerm(String searchTerm, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        String artistSort = sortByArtistFirst ? "artist, " : "";
        query.myQuery =
                "SELECT id, name, artist, album, time, track_number, file FROM track WHERE LCASE(name) LIKE ? ESCAPE '\\' OR LCASE(album) LIKE ? ESCAPE '\\' OR LCASE(artist) LIKE ? ESCAPE '\\' ORDER BY " +
                        artistSort + "album, track_number, name";
        String sqlTerm = null;
        if (StringUtils.isNotEmpty(searchTerm)) {
            sqlTerm = "%" + SQLUtils.escapeLikeString(searchTerm.toLowerCase()) + "%";
        } else {
            sqlTerm = "%";
        }
        query.myParameters = new String[] {sqlTerm, sqlTerm, sqlTerm};
        return query;
    }

    public static FindTrackQuery getForAlbum(String[] albums, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        String artistSort = sortByArtistFirst ? "artist, " : "";
        if (albums.length > 1) {
            query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE album IN (" +
                    SQLUtils.createParameters(albums.length) + ") ORDER BY " + artistSort + "track_number, name";
        } else {
            query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE album = ? ORDER BY " + artistSort +
                    "track_number, name";
        }
        query.myParameters = albums;
        return query;
    }

    public static FindTrackQuery getForArtist(String[] artists, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        String artistSort = sortByArtistFirst ? "artist, " : "";
        if (artists.length > 1) {
            query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE artist IN (" +
                    SQLUtils.createParameters(artists.length) + ") ORDER BY " + artistSort + "album, track_number, name";
        } else {
            query.myQuery = "SELECT id, name, artist, album, time, track_number, file FROM track WHERE artist = ? ORDER BY " + artistSort +
                    "album, track_number, name";
        }
        query.myParameters = artists;
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