/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MediaType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackStatement
 */
public class UpdateTrackStatement implements InsertOrUpdateTrackStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateTrackStatement.class);
    public static final String UNKNOWN = new String("!");

    private TrackSource mySource;
    private String myId;
    private String myName;
    private String myArtist;
    private String myAlbum;
    private int myTime;
    private int myTrackNumber;
    private String myFileName;
    private boolean myProtected;
    private MediaType myMediaType;
    private String myGenre;
    private String myMp4Codec;
    private String myComment;
    private int myPosNumber;
    private int myPosSize;
    private int myYear;
    private SmartStatement myStatement;

    public UpdateTrackStatement(TrackSource source) {
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

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public void setGenre(String genre) {
        myGenre = genre;
    }

    public void setMp4Codec(String mp4Codec) {
        myMp4Codec = mp4Codec;
    }

    public void setComment(String comment) {
        myComment = comment;
    }

    public void setPos(int number, int size) {
        myPosNumber = number;
        myPosSize = size;
    }
    
    public void setYear(int year) {
        myYear = year;
    }

    protected String getStatementName() {
        return "updateTrack";
    }

    public synchronized void execute(Connection connection) throws SQLException {
        try {
            String originalArtist = myArtist;
            myArtist = dropWordsFromArtist(myArtist);
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, getStatementName());
            }
            myStatement.clearParameters();
            myStatement.setString("name", StringUtils.isNotEmpty(myName) ? myName : UpdateTrackStatement.UNKNOWN);
            myStatement.setString("album", StringUtils.isNotEmpty(myAlbum) ? myAlbum : UpdateTrackStatement.UNKNOWN);
            myStatement.setString("artist", StringUtils.isNotEmpty(myArtist) ? myArtist : UpdateTrackStatement.UNKNOWN);
            myStatement.setString("original_artist", StringUtils.isNotEmpty(originalArtist) ? originalArtist : UpdateTrackStatement.UNKNOWN);
            myStatement.setInt("time", myTime);
            myStatement.setInt("track_number", myTrackNumber);
            myStatement.setString("file", myFileName);
            myStatement.setString("id", myId);
            myStatement.setBoolean("protected", myProtected);
            myStatement.setString("mediatype", myMediaType.name());
            myStatement.setString("genre", myGenre);
            if (mySource == TrackSource.YouTube) {
                myStatement.setString("suffix", "swf");
            } else if (mySource.isExternal()) {
                myStatement.setString("suffix", "");
            } else {
                myStatement.setString("suffix", FileSupportUtils.getFileSuffix(myFileName));
            }
            myStatement.setString("mp4codec", myMp4Codec);
            myStatement.setLong("ts_updated", System.currentTimeMillis());
            myStatement.setString("comment", myComment);
            myStatement.setInt("pos_number", myPosNumber);
            myStatement.setInt("pos_size", myPosSize);
            myStatement.setInt("year", myYear);
            myStatement.execute();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not update track with ID \"%s\" in database.", myId), e);
            }
        }
    }

    static String dropWordsFromArtist(String artist) {
        String dropWords = MyTunesRss.CONFIG.getArtistDropWords();
        if (StringUtils.isNotEmpty(dropWords) && StringUtils.isNotEmpty(artist)) {
            for (StringTokenizer tokenizer = new StringTokenizer(dropWords, ","); tokenizer.hasMoreTokens();) {
                String word = tokenizer.nextToken().toLowerCase();
                while (artist.toLowerCase().startsWith(word + " ")) {
                    artist = artist.substring(word.length()).trim();
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
        myMediaType = MediaType.Other;
        myGenre = null;
        myMp4Codec = null;
    }
}