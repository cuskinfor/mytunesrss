/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<Collection<Track>> {
    private static final Log LOG = LogFactory.getLog(FindPlaylistTracksQuery.class);

    private static final String BASE_SQL =
            "SELECT ltp.index AS index, t.id AS id, t.name AS name, t.artist AS artist, t.album AS album, t.time AS time, t.track_number AS track_number, t.file AS file, t.protected AS protected, t.video AS video FROM link_track_playlist ltp, track t WHERE t.id = ltp.track_id AND ltp.playlist_id = ?";
    private static final String BASE_SQL_WITH_LIMIT = BASE_SQL + " AND ltp.index >= ? AND ltp.index <= ?";

    private static final String ORDER_PLAYLIST = " ORDER BY index";
    private static final String ORDER_ALBUM = " ORDER BY album, track_number, name";
    private static final String ORDER_ARTIST = " ORDER BY artist, album, track_number, name";

    private static final String QUERY_PLAYLIST_ORDER = BASE_SQL + ORDER_PLAYLIST;
    private static final String QUERY_LIMITED_PLAYLIST_ORDER = BASE_SQL_WITH_LIMIT + ORDER_PLAYLIST;
    private static final String QUERY_ALBUM_ORDER = BASE_SQL + ORDER_ALBUM;
    private static final String QUERY_LIMITED_ALBUM_ORDER = BASE_SQL_WITH_LIMIT + ORDER_ALBUM;;
    private static final String QUERY_ARTIST_ORDER = BASE_SQL + ORDER_ARTIST;
    private static final String QUERY_LIMITED_ARTIST_ORDER = BASE_SQL_WITH_LIMIT + ORDER_ARTIST;

    private FindPlaylistTracksQuery.TrackResultBuilder myBuilder = new FindPlaylistTracksQuery.TrackResultBuilder();
    private String mySql;
    private Object[] myParameters;

    public FindPlaylistTracksQuery(String id) {
        myParameters = StringUtils.split(id, "_");
        mySql = myParameters.length == 3 ? QUERY_LIMITED_PLAYLIST_ORDER : QUERY_PLAYLIST_ORDER;
    }

    public FindPlaylistTracksQuery(String id, boolean sortByArtist) {
        myParameters = StringUtils.split(id, "_");
        mySql = sortByArtist ? (myParameters.length == 3 ? QUERY_LIMITED_ARTIST_ORDER : QUERY_ARTIST_ORDER) : (myParameters.length == 3 ?
                QUERY_LIMITED_ALBUM_ORDER : QUERY_ALBUM_ORDER);
    }

    public Collection<Track> execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(mySql);
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
            track.setProtected(resultSet.getBoolean("PROTECTED"));
            track.setVideo(resultSet.getBoolean("VIDEO"));
            return track;
        }
    }
}