/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindSmartPlaylistQuery
 */
public class FindSmartPlaylistQuery extends DataStoreQuery<DataStoreQuery.QueryResult<SmartPlaylist>> {
    private String myId;

    public FindSmartPlaylistQuery(String id) {
        myId = id;
    }

    public QueryResult<SmartPlaylist> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findSmartPlaylistById");
        statement.setString("id", myId);
        return execute(statement, new SmartPlaylistResultBuilder(myId));
    }

    public static class SmartPlaylistResultBuilder implements ResultBuilder<SmartPlaylist> {
        private String myId;

        private SmartPlaylistResultBuilder(String id) {
            myId = id;
        }

        public SmartPlaylist create(ResultSet resultSet) throws SQLException {
            Playlist playlist = new Playlist();
            SmartInfo smartInfo = new SmartInfo();
            SmartPlaylist smartPlaylist = new SmartPlaylist();
            smartPlaylist.setPlaylist(playlist);
            smartPlaylist.setSmartInfo(smartInfo);
            playlist.setId(myId);
            playlist.setName(resultSet.getString("NAME"));
            playlist.setType(PlaylistType.MyTunesSmart);
            playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            playlist.setUserPrivate(resultSet.getBoolean("USER_PRIVATE"));
            playlist.setHidden(resultSet.getBoolean("HIDDEN"));
            playlist.setUserOwner(resultSet.getString("USER_OWNER"));
            playlist.setContainerId(resultSet.getString("CONTAINER_ID"));
            smartInfo.setAlbumPattern(resultSet.getString("ALBUM_PATTERN"));
            smartInfo.setArtistPattern(resultSet.getString("ARTIST_PATTERN"));
            smartInfo.setFilePattern(resultSet.getString("FILE_PATTERN"));
            smartInfo.setGenrePattern(resultSet.getString("GENRE_PATTERN"));
            smartInfo.setProtected(resultSet.getBoolean("PROTECTED"));
            if (resultSet.wasNull()) {
                smartInfo.setProtected(null);
            }
            smartInfo.setTimeMax(resultSet.getInt("TIME_MAX"));
            if (resultSet.wasNull()) {
                smartInfo.setTimeMax(null);
            }
            smartInfo.setTimeMin(resultSet.getInt("TIME_MIN"));
            if (resultSet.wasNull()) {
                smartInfo.setTimeMin(null);
            }
            smartInfo.setTitlePattern(resultSet.getString("TITLE_PATTERN"));
            String mediaTypeName = resultSet.getString("MEDIATYPE");
            if (StringUtils.isNotBlank(mediaTypeName)) {
                smartInfo.setMediaType(MediaType.valueOf(resultSet.getString("MEDIATYPE")));
            }
            return smartPlaylist;
        }
    }
}