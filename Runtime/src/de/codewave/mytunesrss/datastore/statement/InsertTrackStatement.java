/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.sql.*;

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
    private boolean myVideo;
    private String myGenre;
    private TrackSource mySource;
    private SmartStatement myStatement;

    public InsertTrackStatement(TrackSource source) {
        mySource = source;
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

    public void setGenre(String genre) {
        myGenre = genre;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        try {
            myArtist = UpdateTrackStatement.dropWordsFromArtist(myArtist);
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, "insertTrack");
            }
            myStatement.clearParameters();
            myStatement.setString("id", myId);
            myStatement.setString("name", StringUtils.isNotEmpty(myName) ? myName : UNKNOWN);
            myStatement.setString("artist", StringUtils.isNotEmpty(myArtist) ? myArtist : UNKNOWN);
            myStatement.setString("album", StringUtils.isNotEmpty(myAlbum) ? myAlbum : UNKNOWN);
            myStatement.setInt("time", myTime);
            myStatement.setInt("track_number", myTrackNumber);
            myStatement.setString("file", myFileName);
            myStatement.setBoolean("protected", myProtected);
            myStatement.setBoolean("video", myVideo);
            myStatement.setString("source", mySource.name());
            myStatement.setString("genre", myGenre);
            myStatement.setString("suffix", FileSupportUtils.getFileSuffix(myFileName));
            myStatement.execute();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not insert track with ID \"%s\" into database.", myId), e);
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
        myGenre = null;
    }
}