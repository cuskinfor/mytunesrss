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
    private String myExecutedId;

    public SaveMyTunesSmartPlaylistStatement(String userName, boolean userPrivate, SmartInfo smartInfo) {
        super(null);
        setType(PlaylistType.MyTunesSmart);
        setUserName(userName);
        setUserPrivate(userPrivate);
        mySmartInfo = smartInfo;
    }

    public void execute(Connection connection) throws SQLException {
        handleIdAndUpdate(connection);
        super.execute(connection);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, isUpdate() ? "updateSmartInfo" : "insertSmartInfo");
        statement.setString("playlist_id", getId());
        statement.setString("album_pattern", mySmartInfo.getAlbumPattern());
        statement.setString("artist_pattern", mySmartInfo.getArtistPattern());
        statement.setString("genre_pattern", mySmartInfo.getGenrePattern());
        statement.setString("series_pattern", mySmartInfo.getSeriesPattern());
        statement.setString("title_pattern", mySmartInfo.getTitlePattern());
        statement.setString("file_pattern", mySmartInfo.getFilePattern());
        statement.setString("tag_pattern", mySmartInfo.getTagPattern());
        statement.setString("comment_pattern", mySmartInfo.getCommentPattern());
        statement.setString("composer_pattern", mySmartInfo.getComposerPattern());
        statement.setString("source_id", mySmartInfo.getSourceId());
        if (mySmartInfo.getTimeMin() != null) {
            statement.setInt("time_min", mySmartInfo.getTimeMin());
        }
        if (mySmartInfo.getTimeMax() != null) {
            statement.setInt("time_max", mySmartInfo.getTimeMax());
        }
        if (mySmartInfo.getProtected() != null) {
            statement.setBoolean("protected", mySmartInfo.getProtected());
        }
        if (mySmartInfo.getMediaType() != null) {
            statement.setString("mediatype", mySmartInfo.getMediaType().name());
        }
        if (mySmartInfo.getVideoType() != null) {
            statement.setString("videotype", mySmartInfo.getVideoType().name());
        }
        statement.execute();
        myExecutedId = getId();
    }

    public String getPlaylistIdAfterExecute() {
        if (myExecutedId == null) {
            throw new IllegalStateException("Statement not yet executed.");
        }
        return myExecutedId;
    }

    protected void handleIdAndUpdate(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "nextPlaylistId").executeQuery();
            if (resultSet.next()) {
                int playlistId = resultSet.getInt("ID");
                setId("MyTunesRSS" + playlistId);
            }
        } else {
            setUpdate(true);
        }
    }
}