/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.utils.sql.ResultBuilder;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SmartPlaylistResultBuilder implements ResultBuilder<SmartPlaylist> {

    public SmartPlaylist create(ResultSet resultSet) throws SQLException {
        Playlist playlist = new Playlist();
        SmartInfo smartInfo = new SmartInfo();
        SmartPlaylist smartPlaylist = new SmartPlaylist();
        smartPlaylist.setPlaylist(playlist);
        smartPlaylist.setSmartInfo(smartInfo);
        playlist.setId(resultSet.getString("ID"));
        playlist.setName(resultSet.getString("NAME"));
        playlist.setType(PlaylistType.MyTunesSmart);
        playlist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
        playlist.setUserPrivate(resultSet.getBoolean("USER_PRIVATE"));
        playlist.setHidden(resultSet.getBoolean("HIDDEN"));
        playlist.setUserOwner(resultSet.getString("USER_OWNER"));
        playlist.setContainerId(resultSet.getString("CONTAINER_ID"));
        smartInfo.setAlbumPattern(resultSet.getString("ALBUM_PATTERN"));
        smartInfo.setArtistPattern(resultSet.getString("ARTIST_PATTERN"));
        smartInfo.setSeriesPattern(resultSet.getString("SERIES_PATTERN"));
        smartInfo.setFilePattern(resultSet.getString("FILE_PATTERN"));
        smartInfo.setTagPattern(resultSet.getString("TAG_PATTERN"));
        smartInfo.setCommentPattern(resultSet.getString("COMMENT_PATTERN"));
        smartInfo.setGenrePattern(resultSet.getString("GENRE_PATTERN"));
        smartInfo.setComposerPattern(resultSet.getString("COMPOSER_PATTERN"));
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
            smartInfo.setMediaType(MediaType.valueOf(mediaTypeName));
        }
        String videoTypeName = resultSet.getString("VIDEOTYPE");
        if (StringUtils.isNotBlank(videoTypeName)) {
            smartInfo.setVideoType(VideoType.valueOf(videoTypeName));
        }
        return smartPlaylist;
    }
}
