/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackStatement
 */
public class UpdateTrackStatement implements InsertOrUpdateTrackStatement {
    private static final Log LOG = LogFactory.getLog(UpdateTrackStatement.class);
    public static final String UNKNOWN = new String("!");

    private String myId;
    private String myName;
    private String myArtist;
    private String myAlbum;
    private int myTime;
    private int myTrackNumber;
    private String myFileName;
    private boolean myProtected;
    private boolean myVideo;
    private PreparedStatement myStatement;
    private static final String SQL =
            "UPDATE track SET name = ?, album = ?, artist = ?, time = ?, track_number = ?, file = ?, protected = ?, video = ? WHERE id = ?";

    public UpdateTrackStatement() {
        // intentionally left blank
    }

    public UpdateTrackStatement(DataStoreSession storeSession) {
        try {
            myStatement = storeSession.prepare(UpdateTrackStatement.SQL);
        } catch (SQLException e) {
            if (UpdateTrackStatement.LOG.isErrorEnabled()) {
                UpdateTrackStatement.LOG.error("Could not prepare statement, trying again during execution.", e);
            }
        }
    }

    public void setAlbum(String album) {
        myAlbum = album;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    public void setFileName(String fileName) {
        myFileName = fileName;
    }

    public void setId(String id) {
        myId = id;
    }

    public void setName(String name) {
        myName = name;
    }

    public void setTime(int time) {
        myTime = time;
    }

    public void setTrackNumber(int trackNumber) {
        myTrackNumber = trackNumber;
    }

    public void setProtected(boolean aProtected) {
        myProtected = aProtected;
    }

    public void setVideo(boolean video) {
        myVideo = video;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            PreparedStatement statement = myStatement != null ? myStatement : connection.prepareStatement(UpdateTrackStatement.SQL);
            statement.clearParameters();
            statement.setString(1, StringUtils.isNotEmpty(myName) ? myName : UpdateTrackStatement.UNKNOWN);
            statement.setString(2, StringUtils.isNotEmpty(myAlbum) ? myAlbum : UpdateTrackStatement.UNKNOWN);
            statement.setString(3, StringUtils.isNotEmpty(myArtist) ? myArtist : UpdateTrackStatement.UNKNOWN);
            statement.setInt(4, myTime);
            statement.setInt(5, myTrackNumber);
            statement.setString(6, myFileName);
            statement.setString(7, myId);
            statement.setBoolean(8, myProtected);
            statement.setBoolean(9, myVideo);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (UpdateTrackStatement.LOG.isErrorEnabled()) {
                UpdateTrackStatement.LOG.error(String.format("Could not update track with ID \"%s\" in database.", myId), e);
            }
        }
    }

    public void clear() {
        myId = null;
        myName = null;
        myArtist = null;
        myAlbum = null;
        myTime = 0;
        myTrackNumber = 0;
        myFileName = null;
        myProtected = false;
        myVideo = false;
    }
}