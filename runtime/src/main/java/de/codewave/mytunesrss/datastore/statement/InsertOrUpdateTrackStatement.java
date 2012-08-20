/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.*;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertOrUpdateTrackStatement
 */
public abstract class InsertOrUpdateTrackStatement implements DataStoreStatement {

    static String dropWordsFromArtist(String artist, String dropWords) {
        if (StringUtils.isNotBlank(dropWords) && StringUtils.isNotBlank(artist)) {
            for (StringTokenizer tokenizer = new StringTokenizer(dropWords, ","); tokenizer.hasMoreTokens();) {
                String word = tokenizer.nextToken().toLowerCase();
                while (artist.toLowerCase().startsWith(word + " ")) {
                    artist = artist.substring(word.length()).trim();
                }
            }
        }
        return artist;
    }

    public static final String UNKNOWN = new String("!");

    private String myId;
    private String myName;
    private String myArtist;
    private String myAlbumArtist;
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
    private VideoType myVideoType;
    private String mySeries;
    private int mySeason;
    private int myEpisode;
    private TrackSource mySource;
    private String myComposer;
    private boolean myCompilation;
    private String mySourceId;
    private SmartStatement myStatement;

    protected InsertOrUpdateTrackStatement(TrackSource source, String sourceId) {
        mySource = source;
        mySourceId = sourceId;
    }

    public void setAlbum(String album) {
        myAlbum = album;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    public void setAlbumArtist(String albumArtist) {
        myAlbumArtist = albumArtist;
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

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public void setSeries(String series) {
        mySeries = series;
    }

    public void setSeason(int season) {
        mySeason = season;
    }

    public void setEpisode(int episode) {
        myEpisode = episode;
    }

    public void setComposer(String composer) {
        myComposer = composer;
    }

    public void setCompilation(boolean compilation) {
        myCompilation = compilation;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        try {
            String originalArtist = myArtist;
            String originalAlbumArtist = myAlbumArtist;
            DatasourceConfig config = MyTunesRss.CONFIG.getDatasource(mySourceId);
            String dropWords = config instanceof AudioVideoDatasourceConfig ? ((AudioVideoDatasourceConfig)config).getArtistDropWords() : null;
            myArtist = UpdateTrackStatement.dropWordsFromArtist(myArtist, dropWords);
            myAlbumArtist = UpdateTrackStatement.dropWordsFromArtist(myAlbumArtist, dropWords);
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, getStatementName());
            }
            myStatement.clearParameters();
            myStatement.setString("id", myId);
            myStatement.setString("name", StringUtils.isNotEmpty(myName) ? myName : UNKNOWN);
            myStatement.setString("artist", StringUtils.isNotEmpty(myArtist) ? myArtist : UNKNOWN);
            myStatement.setString("original_artist", StringUtils.isNotEmpty(originalArtist) ? originalArtist : UNKNOWN);
            myStatement.setString("album_artist", StringUtils.isNotEmpty(myAlbumArtist) ? myAlbumArtist : UNKNOWN);
            myStatement.setString("original_album_artist", StringUtils.isNotEmpty(originalAlbumArtist) ? originalAlbumArtist : UNKNOWN);
            myStatement.setString("album", StringUtils.isNotEmpty(myAlbum) ? myAlbum : UNKNOWN);
            myStatement.setInt("time", myTime);
            myStatement.setInt("track_number", myTrackNumber);
            myStatement.setString("file", myFileName);
            myStatement.setBoolean("protected", myProtected);
            myStatement.setString("mediatype", myMediaType.name());
            myStatement.setString("source", mySource.name());
            myStatement.setString("genre", myGenre);
            myStatement.setString("suffix", FileSupportUtils.getFileSuffix(myFileName));
            myStatement.setString("mp4codec", myMp4Codec);
            myStatement.setLong("ts_updated", System.currentTimeMillis());
            myStatement.setLong("playcount", 0);
            myStatement.setString("comment", myComment);
            myStatement.setInt("pos_number", myPosNumber);
            myStatement.setInt("pos_size", myPosSize);
            myStatement.setString("videotype", myVideoType != null ? myVideoType.name() : null);
            myStatement.setString("series", mySeries);
            myStatement.setInt("season", mySeason);
            myStatement.setInt("episode", myEpisode);
            myStatement.setInt("year", myYear);
            myStatement.setString("composer", myComposer);
            myStatement.setInt("compilation", myCompilation ? 1 : 0);
            myStatement.setString("source_id", mySourceId);
            myStatement.execute();
        } catch (SQLException e) {
            logError(myId, e);
        }
    }

    protected abstract void logError(String id, SQLException e);

    protected abstract String getStatementName();

    public void clear() {
        myId = null;
        myName = null;
        myArtist = null;
        myAlbumArtist = null;
        myAlbum = null;
        myTime = 0;
        myTrackNumber = 0;
        myFileName = null;
        myProtected = false;
        myMediaType = MediaType.Other;
        myGenre = null;
        myMp4Codec = null;
        myVideoType = null;
        mySeries = null;
        mySeason = 0;
        myEpisode = 0;
        myComposer = null;
        myCompilation = false;
    }
}