/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindTrackQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {
    public static FindTrackQuery getForId(String[] trackIds) {
        FindTrackQuery query = new FindTrackQuery();
        query.myIds = trackIds;
        return query;
    }


    public static FindTrackQuery getForSearchTerm(User user, String searchTerm, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        String[] searchTerms = StringUtils.split(searchTerm, " ");
        if (searchTerms == null) {
            searchTerms = new String[] {searchTerm};
        }
        for (int i = 0; i < searchTerms.length; i++) {
            if (StringUtils.isNotEmpty(searchTerms[i])) {
                searchTerms[i] = "%" + SQLUtils.escapeLikeString(searchTerms[i].toLowerCase(), "\\") + "%";
            } else {
                searchTerms[i] = "%";
            }
        }
        query.mySearchTerms = searchTerms;
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    public static FindTrackQuery getForAlbum(User user, String[] albums, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myAlbums = albums;
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    public static FindTrackQuery getForArtist(User user, String[] artists, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myArtists = artists;
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    public static FindTrackQuery getForGenre(User user, String[] genres, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myGenres = genres;
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    private String[] myIds;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private String[] mySearchTerms;
    private boolean myArtistSort;
    private String myRestrictedPlaylistId;

    private FindTrackQuery() {
        // intentionally left blank
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        String suffix = StringUtils.isEmpty(myRestrictedPlaylistId) ? "" : "Restricted";
        if (myArtistSort) {
            statement = MyTunesRssUtils.createStatement(connection, "findTracksWithArtistOrder" + suffix);
        } else {
            statement = MyTunesRssUtils.createStatement(connection, "findTracks" + suffix);
        }
        statement.setItems("id", myIds);
        statement.setItems("album", myAlbums);
        statement.setItems("artist", myArtists);
        statement.setItems("genre", myGenres);
        statement.setItems("search", mySearchTerms);
        statement.setString("restrictedPlaylistId", myRestrictedPlaylistId);
        return execute(statement, new TrackResultBuilder());
        //        if (myIds != null && myIds.length > 1) {
        //            Map<String, Track> idToTrack = new HashMap<String, Track>(tracks.size());
        //            for (Track track : tracks) {
        //                idToTrack.put(track.getId(), track);
        //            }
        //            tracks.clear();
        //            for (int i = 0; i < myIds.length; i++) {
        //                tracks.add(idToTrack.get(myIds[i]));
        //            }
        //        }
        //        return tracks;
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
            String pathname = resultSet.getString("FILE");
            track.setFile(StringUtils.isNotEmpty(pathname) ? new File(pathname) : null);
            track.setProtected(resultSet.getBoolean("PROTECTED"));
            track.setVideo(resultSet.getBoolean("VIDEO"));
            track.setGenre(resultSet.getString("GENRE"));
            track.setMp4Codec(resultSet.getString("MP4CODEC"));
            track.setTsPlayed(resultSet.getLong("TS_PLAYED"));
            track.setTsUpdated(resultSet.getLong("TS_UPDATED"));
            track.setLastImageUpdate(resultSet.getLong("LAST_IMAGE_UPDATE"));
            track.setPlayCount(resultSet.getLong("PLAYCOUNT"));
            track.setImageCount(resultSet.getInt("IMAGECOUNT"));
            return track;
        }
    }
}