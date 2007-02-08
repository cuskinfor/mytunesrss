/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

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
    private String myGenre;
    private PreparedStatement myStatement;
    private static final String SQL =
            "UPDATE track SET name = ?, album = ?, artist = ?, time = ?, track_number = ?, file = ?, protected = ?, video = ?, genre = ?, suffix = ? WHERE id = ?";

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

    public void setGenre(String genre) {
        myGenre = genre;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            myArtist = dropWordsFromArtist(myArtist);
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
            statement.setString(10, myGenre);
            statement.setString(11, FileSupportUtils.getFileSuffix(myFileName));
            statement.executeUpdate();
        } catch (SQLException e) {
            if (UpdateTrackStatement.LOG.isErrorEnabled()) {
                UpdateTrackStatement.LOG.error(String.format("Could not update track with ID \"%s\" in database.", myId), e);
            }
        }
    }

    static String dropWordsFromArtist(String artist) {
        String dropWords = MyTunesRss.CONFIG.getArtistDropWords();
        if (StringUtils.isNotEmpty(dropWords) && StringUtils.isNotEmpty(artist)) {
            for (StringTokenizer tokenizer = new StringTokenizer(dropWords, ","); tokenizer.hasMoreTokens(); ) {
                String word = tokenizer.nextToken().toLowerCase();
                while (artist.toLowerCase().startsWith(word + " ")) {
                    artist = artist.substring(word.length() + 1);
                }
            }
        }
        return artist;
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