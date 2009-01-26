/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveMyTunesSmartPlaylistStatement extends SavePlaylistStatement {
    private SmartInfo mySmartInfo;

    public SaveMyTunesSmartPlaylistStatement(String userName, boolean userPrivate, SmartInfo smartInfo) {
        setType(PlaylistType.MyTunesSmart);
        setUserName(userName);
        setUserPrivate(userPrivate);
        mySmartInfo = smartInfo;
    }

    public void execute(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "nextPlaylistId").executeQuery();
            if (resultSet.next()) {
                int playlistId = resultSet.getInt("ID");
                setId("MyTunesRSS" + playlistId);
            }
        } else {
            setUpdate(true);
        }
        super.execute(connection);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, isUpdate() ? "updateSmartInfo" : "insertSmartInfo");
        statement.setString("playlist_id", getId());
        statement.setString("album_pattern", mySmartInfo.getAlbumPattern());
        statement.setString("artist_pattern", mySmartInfo.getArtistPattern());
        statement.setString("genre_pattern", mySmartInfo.getGenrePattern());
        statement.setString("title_pattern", mySmartInfo.getTitlePattern());
        statement.setString("file_pattern", mySmartInfo.getFilePattern());
        statement.setInt("time_min", mySmartInfo.getTimeMin());
        statement.setInt("time_max", mySmartInfo.getTimeMax());
        statement.setBoolean("protected", mySmartInfo.getProtected());
        statement.setBoolean("video", mySmartInfo.getVideo());
    }
}