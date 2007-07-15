/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
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
    private SmartStatement myStatement;

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
            myArtist = dropWordsFromArtist(myArtist);
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, "updateTrack");
            }
            myStatement.clearParameters();
            myStatement.setString("name", StringUtils.isNotEmpty(myName) ? myName : UpdateTrackStatement.UNKNOWN);
            myStatement.setString("album", StringUtils.isNotEmpty(myAlbum) ? myAlbum : UpdateTrackStatement.UNKNOWN);
            myStatement.setString("artist", StringUtils.isNotEmpty(myArtist) ? myArtist : UpdateTrackStatement.UNKNOWN);
            myStatement.setInt("time", myTime);
            myStatement.setInt("track_number", myTrackNumber);
            myStatement.setString("file", myFileName);
            myStatement.setString("id", myId);
            myStatement.setBoolean("protected", myProtected);
            myStatement.setBoolean("video", myVideo);
            myStatement.setString("genre", myGenre);
            myStatement.setString("suffix", FileSupportUtils.getFileSuffix(myFileName));
            myStatement.execute();
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