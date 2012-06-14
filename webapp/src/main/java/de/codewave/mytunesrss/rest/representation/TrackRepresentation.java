/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.TrackSource;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
public class TrackRepresentation {

    private URI myImageUri;
    private URI myM3uUri;
    private URI myXspfUri;
    private URI myRssUri;
    private URI myDownloadUri;
    private URI myPlaybackUri;
    private URI myTagsUri;
    private String myId;
    private String myAlbum;
    private String myAlbumArtist;
    private String myArtist;
    private String myComment;
    private String myComposer;
    private int myEpisode;
    private String myFilename;
    private String myGenre;
    private String myImageHash;
    private long myLastImageUpdate;
    private MediaType myMediaType;
    private String myMp4Codec;
    private String myName;
    private String myOriginalArtist;
    private long myPlayCount;
    private int myPosNumber;
    private int myPosSize;
    private boolean myProtected;
    private long myTsPlayed;
    private long myTsUpdated;
    private int mySeason;
    private String myTvShow;
    private TrackSource mySource;
    private int myTime;
    private int myTrackNumber;
    private VideoType myVideoType;
    private int myYear;

    public TrackRepresentation() {
    }

    public TrackRepresentation(Track track) {
        setId(track.getId());
        setAlbum(track.getAlbum());
        setAlbumArtist(track.getAlbumArtist());
        setArtist(track.getArtist());
        setComment(track.getComment());
        setComposer(track.getComposer());
        setEpisode(track.getEpisode());
        setFilename(track.getFilename());
        setGenre(track.getGenre());
        setImageHash(track.getImageHash());
        setLastImageUpdate(track.getLastImageUpdate());
        setMediaType(track.getMediaType());
        setMp4Codec(track.getMp4Codec());
        setName(track.getName());
        setOriginalArtist(track.getOriginalArtist());
        setPlayCount(track.getPlayCount());
        setPosNumber(track.getPosNumber());
        setPosSize(track.getPosSize());
        setProtected(track.isProtected());
        setTsPlayed(track.getTsPlayed());
        setTsUpdated(track.getTsUpdated());
        setSeason(track.getSeason());
        setTvShow(track.getSeries());
        setSource(track.getSource());
        setTime(track.getTime());
        setTrackNumber(track.getTrackNumber());
        setVideoType(track.getVideoType());
        setYear(track.getYear());
    }

    public URI getImageUri() {
        return myImageUri;
    }

    public void setImageUri(URI imageUri) {
        myImageUri = imageUri;
    }

    public URI getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(URI m3uUri) {
        myM3uUri = m3uUri;
    }

    public URI getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(URI xspfUri) {
        myXspfUri = xspfUri;
    }

    public URI getRssUri() {
        return myRssUri;
    }

    public void setRssUri(URI rssUri) {
        myRssUri = rssUri;
    }

    public URI getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(URI downloadUri) {
        myDownloadUri = downloadUri;
    }

    public URI getPlaybackUri() {
        return myPlaybackUri;
    }

    public void setPlaybackUri(URI playbackUri) {
        myPlaybackUri = playbackUri;
    }

    public URI getTagsUri() {
        return myTagsUri;
    }

    public void setTagsUri(URI tagsUri) {
        myTagsUri = tagsUri;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public String getAlbum() {
        return myAlbum;
    }

    public void setAlbum(String album) {
        myAlbum = album;
    }

    public String getAlbumArtist() {
        return myAlbumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        myAlbumArtist = albumArtist;
    }

    public String getArtist() {
        return myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    public String getComment() {
        return myComment;
    }

    public void setComment(String comment) {
        myComment = comment;
    }

    public String getComposer() {
        return myComposer;
    }

    public void setComposer(String composer) {
        myComposer = composer;
    }

    public int getEpisode() {
        return myEpisode;
    }

    public void setEpisode(int episode) {
        myEpisode = episode;
    }

    public String getFilename() {
        return myFilename;
    }

    public void setFilename(String filename) {
        myFilename = filename;
    }

    public String getGenre() {
        return myGenre;
    }

    public void setGenre(String genre) {
        myGenre = genre;
    }

    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    public long getLastImageUpdate() {
        return myLastImageUpdate;
    }

    public void setLastImageUpdate(long lastImageUpdate) {
        myLastImageUpdate = lastImageUpdate;
    }

    public MediaType getMediaType() {
        return myMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public String getMp4Codec() {
        return myMp4Codec;
    }

    public void setMp4Codec(String mp4Codec) {
        myMp4Codec = mp4Codec;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getOriginalArtist() {
        return myOriginalArtist;
    }

    public void setOriginalArtist(String originalArtist) {
        myOriginalArtist = originalArtist;
    }

    public long getPlayCount() {
        return myPlayCount;
    }

    public void setPlayCount(long playCount) {
        myPlayCount = playCount;
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

    public boolean isProtected() {
        return myProtected;
    }

    public void setProtected(boolean aProtected) {
        myProtected = aProtected;
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

    public int getSeason() {
        return mySeason;
    }

    public void setSeason(int season) {
        mySeason = season;
    }

    public String getTvShow() {
        return myTvShow;
    }

    public void setTvShow(String tvShow) {
        myTvShow = tvShow;
    }

    public TrackSource getSource() {
        return mySource;
    }

    public void setSource(TrackSource source) {
        mySource = source;
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

    public VideoType getVideoType() {
        return myVideoType;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public int getYear() {
        return myYear;
    }

    public void setYear(int year) {
        myYear = year;
    }
}
