/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.CommonTrackDatasourceConfig;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.lucene.LuceneTrack;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertOrUpdateTrackStatement
 */
public abstract class InsertOrUpdateTrackStatement implements DataStoreStatement {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertOrUpdateTrackStatement.class);

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

    public static final String UNKNOWN = "";

    private String myId;
    private String myName;
    private String myArtist;
    private String myAlbumArtist;
    private String mySortAlbumArtist;
    private String myAlbum;
    private String mySortAlbum;
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
    private String myContentType;
    private SmartStatement myStatement;

    protected InsertOrUpdateTrackStatement(TrackSource source, String sourceId) {
        mySource = source;
        mySourceId = sourceId;
    }

    public void setAlbum(String album) {
        myAlbum = stripAsciiControl(album);
    }

    public void setSortAlbum(String sortAlbum) {
        mySortAlbum = stripAsciiControl(sortAlbum);
    }

    public void setArtist(String artist) {
        myArtist = stripAsciiControl(artist);
    }

    public void setAlbumArtist(String albumArtist) {
        myAlbumArtist = stripAsciiControl(albumArtist);
    }

    public void setSortAlbumArtist(String sortAlbumArtist) {
        mySortAlbumArtist = stripAsciiControl(sortAlbumArtist);
    }

    public void setFileName(String fileName) {
        myFileName = fileName;
    }

    public void setId(String id) {
        myId = id;
    }

    public void setName(String name) {
        myName = stripAsciiControl(name);
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

    public boolean isProtected() {
        return myProtected;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public void setGenre(String genre) {
        myGenre = stripAsciiControl(genre);
    }

    public void setMp4Codec(String mp4Codec) {
        myMp4Codec = mp4Codec;
    }

    public void setComment(String comment) {
        myComment = stripAsciiControl(comment);
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
        mySeries = stripAsciiControl(series);
    }

    public void setSeason(int season) {
        mySeason = season;
    }

    public void setEpisode(int episode) {
        myEpisode = episode;
    }

    public void setComposer(String composer) {
        myComposer = stripAsciiControl(composer);
    }

    public void setCompilation(boolean compilation) {
        myCompilation = compilation;
    }

    public void setContentType(String contentType) {
        myContentType = contentType;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        try {
            DatasourceConfig config = MyTunesRss.CONFIG.getDatasource(mySourceId);
            String dropWords = config instanceof CommonTrackDatasourceConfig ? ((CommonTrackDatasourceConfig)config).getArtistDropWords() : null;
            myArtist = UpdateTrackStatement.dropWordsFromArtist(myArtist, dropWords);
            myAlbumArtist = UpdateTrackStatement.dropWordsFromArtist(myAlbumArtist, dropWords);
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, getStatementName());
            }
            myStatement.clearParameters();
            myStatement.setString("id", myId);
            myStatement.setString("name", StringUtils.isNotBlank(myName) ? myName.trim() : UNKNOWN);
            myStatement.setString("artist", StringUtils.isNotBlank(myArtist) ? myArtist.trim() : UNKNOWN);
            String albumArtist = StringUtils.isNotBlank(myAlbumArtist) ? myAlbumArtist.trim() : UNKNOWN;
            myStatement.setString("album_artist", albumArtist);
            myStatement.setString("sort_album_artist", StringUtils.isNotBlank(mySortAlbumArtist) ? mySortAlbumArtist.trim() : albumArtist);
            String album = StringUtils.isNotBlank(myAlbum) ? myAlbum.trim() : UNKNOWN;
            myStatement.setString("album", album);
            myStatement.setString("sort_album", StringUtils.isNotBlank(mySortAlbum) ? mySortAlbum.trim() : album);
            myStatement.setInt("time", myTime);
            myStatement.setInt("track_number", myTrackNumber);
            myStatement.setString("file", myFileName);
            myStatement.setBoolean("protected", myProtected);
            myStatement.setString("mediatype", myMediaType.name());
            myStatement.setString("content_type", myContentType);
            myStatement.setString("source", mySource.name());
            myStatement.setString("genre", StringUtils.defaultIfBlank(MyTunesRss.CONFIG.getGenreMapping(myGenre), myGenre));
            myStatement.setString("original_genre", myGenre);
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
            updateLuceneIndex();
        } catch (SQLException e) {
            logError(myId, e);
        }
    }

    private void updateLuceneIndex() {
        LuceneTrack luceneTrack = newLuceneTrack();
        luceneTrack.setId(myId);
        luceneTrack.setSourceId(mySourceId);
        luceneTrack.setAlbum(myAlbum);
        luceneTrack.setAlbumArtist(myAlbumArtist);
        luceneTrack.setArtist(myArtist);
        luceneTrack.setComment(myComment);
        luceneTrack.setComposer(myComposer);
        luceneTrack.setFilename(myFileName);
        luceneTrack.setGenre(myGenre);
        luceneTrack.setName(myName);
        luceneTrack.setSeries(mySeries);
        MyTunesRss.LUCENE_TRACK_SERVICE.updateTrack(luceneTrack);
    }

    protected abstract LuceneTrack newLuceneTrack();

    protected abstract void logError(String id, SQLException e);

    protected abstract String getStatementName();

    public void clear() {
        myId = null;
        myName = null;
        myArtist = null;
        myAlbumArtist = null;
        mySortAlbumArtist = null;
        myAlbum = null;
        mySortAlbum = null;
        myTime = 0;
        myTrackNumber = 0;
        myFileName = null;
        myProtected = false;
        myMediaType = MediaType.Other;
        myContentType = "application/octet-stream";
        myGenre = null;
        myMp4Codec = null;
        myVideoType = null;
        mySeries = null;
        mySeason = 0;
        myEpisode = 0;
        myComposer = null;
        myCompilation = false;
    }

    private String stripAsciiControl(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : s.toCharArray()) {
            if ((c >= 32 && c != 127) || c == 10 || c == 13) {
                stringBuilder.append(c);
            } else {
                LOGGER.debug("Stripped ASCII control " + (int)c + " from \"" + s + "\".");
            }
        }
        return stringBuilder.toString();
    }
}
