/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<Collection<Track>> {
    public static final String PSEUDO_ID_ALL_BY_ARTIST = "PlaylistAllByArtist";
    public static final String PSEUDO_ID_ALL_BY_ALBUM = "PlaylistAllByAlbum";
    public static final String PSEUDO_ID_RANDOM = "PlaylistRandom";

    public static enum SortOrder {
        Album(), Artist()
    }

    private String myId;
    private SortOrder mySortOrder;

    public FindPlaylistTracksQuery(String id, SortOrder sortOrder) {
        myId = id;
        mySortOrder = sortOrder;
    }

    public Collection<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        if (PSEUDO_ID_ALL_BY_ALBUM.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracksOrderedByAlbum");
            myId = null;
        } else if (PSEUDO_ID_ALL_BY_ARTIST.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracksOrderedByArtist");
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_RANDOM)) {
            statement = MyTunesRssUtils.createStatement(connection, "findRandomTracks");
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            if (splitted.length > 2) {
                String sourceId = splitted[2];
                Collection<Playlist> playlists = new FindPlaylistQuery(null, sourceId).execute(connection);
                if (playlists.size() == 1) {
                    Playlist playlist = playlists.iterator().next();
                    statement.setString("sourcePlaylistId", StringUtils.trimToNull(playlist.getId()));
                }
            }
            myId = null;
        } else if (mySortOrder == SortOrder.Album) {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByAlbum");
        } else if (mySortOrder == SortOrder.Artist) {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByArtist");
        } else {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByIndex");
        }
        if (myId != null) {
            String[] parts = StringUtils.split(myId);
            statement.setString("id", parts[0]);
            if (parts.length == 3) {
                statement.setInt("firstIndex", Integer.parseInt(parts[1]));
                statement.setInt("lastIndex", Integer.parseInt(parts[2]));
            }
        }
        return execute(statement, new TrackResultBuilder());
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
            track.setGenre(resultSet.getString("GENRE"));
            return track;
        }
    }
}