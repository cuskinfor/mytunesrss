/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

import de.codewave.mytunesrss.datastore.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackStatement
 */
public class InsertTrackStatement implements InsertOrUpdateTrackStatement {
    private static final Log LOG = LogFactory.getLog(InsertTrackStatement.class);
    public static final String UNKNOWN = new String("!");

    private String myId;
    private String myName;
    private String myArtist;
    private String myAlbum;
    private int myTime;
    private int myTrackNumber;
    private String myFileName;
    private boolean myProtected;
    private PreparedStatement myStatement;
    private static final String SQL = "INSERT INTO track ( id, name, artist, album, time, track_number, file, protected ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";

    public InsertTrackStatement() {
        // intentionally left blank
    }

    public InsertTrackStatement(DataStoreSession storeSession) {
        try {
            myStatement = storeSession.prepare(SQL);
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not prepare statement, trying again during execution.", e);
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

    public void execute(Connection connection) throws SQLException {
        try {
            PreparedStatement statement = myStatement != null ? myStatement : connection.prepareStatement(SQL);
            statement.clearParameters();
            statement.setString(1, myId);
            statement.setString(2, StringUtils.isNotEmpty(myName) ? myName : UNKNOWN);
            statement.setString(3, StringUtils.isNotEmpty(myArtist) ? myArtist : UNKNOWN);
            statement.setString(4, StringUtils.isNotEmpty(myAlbum) ? myAlbum : UNKNOWN);
            statement.setInt(5, myTime);
            statement.setInt(6, myTrackNumber);
            statement.setString(7, myFileName);
            statement.setBoolean(8, myProtected);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not insert track with ID \"%s\" into database.", myId) , e);
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
    }
}