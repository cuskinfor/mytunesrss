/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;

import java.io.File;

/**
 * de.codewave.mytunesrss.datastore.statement.Track
 */
public class Track {
    private String myId;
    private String myName;
    private String myAlbum;
    private String myArtist;
    private int myTime;
    private int myTrackNumber;
    private File myFile;
    private boolean myProtected;
    private MediaType myMediaType;
    private String myGenre;
    private String myMp4Codec;
    private String myImageHash;
    private long myTsPlayed;
    private long myTsUpdated;
    private long myPlayCount;
    private long myLastImageUpdate;
    private String myComment;
    private int myPosNumber;
    private int myPosSize;
    private int myYear;
    private String myFilename;
    private TrackSource mySource;
    private VideoType myVideoType;
    private String mySeries;
    private int mySeason;
    private int myEpisode;
    private String myAlbumArtist;
    private String myComposer;
    private String mySourceId;
    private String myContentType;

    public Track() {
        // default constructor
    }

    public Track(Track source) {
        setSource(source.getSource());
        setId(source.getId());
        setName(source.getName());
        setAlbum(source.getAlbum());
        setArtist(source.getArtist());
        setTime(source.getTime());
        setTrackNumber(source.getTrackNumber());
        setFilename(source.getFilename());
        setFile(source.getFile());
        setProtected(source.isProtected());
        setMediaType(source.getMediaType());
        setGenre(source.getGenre());
        setMp4Codec(source.getMp4Codec());
        setTsPlayed(source.getTsPlayed());
        setTsUpdated(source.getTsUpdated());
        setLastImageUpdate(source.getLastImageUpdate());
        setPlayCount(source.getPlayCount());
        setImageHash(source.getImageHash());
        setComment(source.getComment());
        setPosNumber(source.getPosNumber());
        setPosSize(source.getPosSize());
        setYear(source.getYear());
        setVideoType(source.getVideoType());
        setEpisode(source.getEpisode());
        setSeason(source.getSeason());
        setSeries(source.getSeries());
        setAlbumArtist(source.getAlbumArtist());
        setComposer(source.getComposer());
        setSourceId(source.getSourceId());
        setContentType(source.getContentType());
    }

    public String getAlbum() {
        return myAlbum;
    }

    public void setAlbum(String album) {
        myAlbum = album;
    }

    public String getArtist() {
        return myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    public File getFile() {
        return myFile;
    }

    public void setFile(File file) {
        myFile = file;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getTime() {
        return myTime;
    }

    public void setTime(int time) {
        myTime = time;
    }

    public int getTrackNumber() {
        return myTrackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        myTrackNumber = trackNumber;
    }

    public boolean isProtected() {
        return myProtected;
    }

    public void setProtected(boolean aProtected) {
        myProtected = aProtected;
    }

    public MediaType getMediaType() {
        return myMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public String getGenre() {
        return myGenre;
    }

    public void setGenre(String genre) {
        myGenre = genre;
    }

    public String getMp4Codec() {
        return myMp4Codec;
    }

    public void setMp4Codec(String mp4Codec) {
        myMp4Codec = mp4Codec;
    }

    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    public String getComment() {
        return myComment;
    }

    public void setComment(String comment) {
        myComment = comment;
    }

    public long getTsPlayed() {
        return myTsPlayed;
    }

    public void setTsPlayed(long tsPlayed) {
        myTsPlayed = tsPlayed;
    }

    public long getTsUpdated() {
        return myTsUpdated;
    }

    public void setTsUpdated(long tsUpdated) {
        myTsUpdated = tsUpdated;
    }

    public long getPlayCount() {
        return myPlayCount;
    }

    public void setPlayCount(long playCount) {
        myPlayCount = playCount;
    }

    public long getContentLength() {
        return getFile().length();
    }

    public long getLastImageUpdate() {
        return myLastImageUpdate;
    }

    public void setLastImageUpdate(long lastImageUpdate) {
        myLastImageUpdate = lastImageUpdate;
    }

    public int getPosNumber() {
        return myPosNumber;
    }

    public void setPosNumber(int posNumber) {
        myPosNumber = posNumber;
    }

    public int getPosSize() {
        return myPosSize;
    }

    public void setPosSize(int posSize) {
        myPosSize = posSize;
    }

    public int getYear() {
        return myYear;
    }

    public void setYear(int year) {
        myYear = year;
    }

    public String getFilename() {
        return myFilename;
    }

    public void setFilename(String filename) {
        this.myFilename = filename;
    }

    public TrackSource getSource() {
        return mySource;
    }

    public void setSource(TrackSource source) {
        this.mySource = source;
    }

    public VideoType getVideoType() {
        return myVideoType;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public String getSeries() {
        return mySeries;
    }

    public void setSeries(String series) {
        mySeries = series;
    }

    public int getSeason() {
        return mySeason;
    }

    public void setSeason(int season) {
        mySeason = season;
    }

    public int getEpisode() {
        return myEpisode;
    }

    public void setEpisode(int episode) {
        myEpisode = episode;
    }

    public String getAlbumArtist() {
        return myAlbumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        myAlbumArtist = albumArtist;
    }

    public String getComposer() {
        return myComposer;
    }

    public void setComposer(String composer) {
        myComposer = composer;
    }

    public String getSourceId() {
        return mySourceId;
    }

    public void setSourceId(String sourceId) {
        mySourceId = sourceId;
    }

    public String getContentType() {
        return myContentType;
    }

    public void setContentType(String contentType) {
        myContentType = contentType;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !Track.class.isAssignableFrom(other.getClass())) {
            return false;
        }
        if (getId() == null) {
            return ((Track) other).getId() == null;
        }
        return getId().equals(((Track) other).getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? 0 : getId().hashCode();
    }
}
