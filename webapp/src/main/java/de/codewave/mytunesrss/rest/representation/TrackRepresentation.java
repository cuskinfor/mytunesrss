/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of a track.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TrackRepresentation implements RestRepresentation {

    private URI myImageUri;
    private URI myM3uUri;
    private URI myXspfUri;
    private URI myRssUri;
    private URI myDownloadUri;
    private URI myPlaybackUri;
    private URI myTagsUri;
    private URI myArtistUri;
    private URI myAlbumUri;
    private String myId;
    private String myAlbum;
    private String myAlbumArtist;
    private String myArtist;
    private String myComment;
    private String myComposer;
    private Integer myEpisode;
    private String myFilename;
    private String myGenre;
    private String myImageHash;
    private Long myLastImageUpdate;
    private MediaType myMediaType;
    private String myMp4Codec;
    private String myName;
    private String myOriginalArtist;
    private Long myPlayCount;
    private Integer myDiscNumber;
    private Integer myDiscCount;
    private Boolean myProtected;
    private Long myTsPlayed;
    private Long myTsUpdated;
    private Integer mySeason;
    private String myTvShow;
    private TrackSource mySource;
    private Integer myTime;
    private Integer myTrackNumber;
    private VideoType myVideoType;
    private Integer myYear;

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
        setDiscNumber(track.getPosNumber());
        setDiscCount(track.getPosSize());
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

    /**
     * URI to the image of the track.
     */
    public URI getImageUri() {
        return myImageUri;
    }

    public void setImageUri(URI imageUri) {
        myImageUri = imageUri;
    }

    /**
     * URI to an M3U playlist of the track.
     */
    public URI getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(URI m3uUri) {
        myM3uUri = m3uUri;
    }

    /**
     * URI to an XSPF playlist of the track.
     */
    public URI getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(URI xspfUri) {
        myXspfUri = xspfUri;
    }

    /**
     * URI to an RSS feed of the track.
     */
    public URI getRssUri() {
        return myRssUri;
    }

    public void setRssUri(URI rssUri) {
        myRssUri = rssUri;
    }

    /**
     * Download URI of the track.
     */
    public URI getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(URI downloadUri) {
        myDownloadUri = downloadUri;
    }

    /**
     * Playback URI of the track.
     */
    public URI getPlaybackUri() {
        return myPlaybackUri;
    }

    public void setPlaybackUri(URI playbackUri) {
        myPlaybackUri = playbackUri;
    }

    /**
     * URI to the tags of the track.
     */
    public URI getTagsUri() {
        return myTagsUri;
    }

    public void setTagsUri(URI tagsUri) {
        myTagsUri = tagsUri;
    }

    /**
     * URI to the artist of the track.
     */
    public URI getArtistUri() {
        return myArtistUri;
    }

    public void setArtistUri(URI artistUri) {
        myArtistUri = artistUri;
    }

    /**
     * URI to the album of the track.
     */
    public URI getAlbumUri() {
        return myAlbumUri;
    }

    public void setAlbumUri(URI albumUri) {
        myAlbumUri = albumUri;
    }

    /**
     * Unique ID of the track.
     */
    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    /**
     * Album of the track.
     */
    public String getAlbum() {
        return myAlbum;
    }

    public void setAlbum(String album) {
        myAlbum = album;
    }

    /**
     * Artist of the album of the track.
     */
    public String getAlbumArtist() {
        return myAlbumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        myAlbumArtist = albumArtist;
    }

    /**
     * Artist of the track.
     */
    public String getArtist() {
        return myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    /**
     * Track comment.
     */
    public String getComment() {
        return myComment;
    }

    public void setComment(String comment) {
        myComment = comment;
    }

    /**
     * Track composer.
     */
    public String getComposer() {
        return myComposer;
    }

    public void setComposer(String composer) {
        myComposer = composer;
    }

    /**
     * Episode if the track is a TV show episode.
     */
    public Integer getEpisode() {
        return myEpisode;
    }

    public void setEpisode(Integer episode) {
        myEpisode = episode;
    }

    /**
     * Filename of the track.
     */
    public String getFilename() {
        return myFilename;
    }

    public void setFilename(String filename) {
        myFilename = filename;
    }

    /**
     * Genre of the track.
     */
    public String getGenre() {
        return myGenre;
    }

    public void setGenre(String genre) {
        myGenre = genre;
    }

    /**
     * Unique hash of the image of the track.
     */
    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    /**
     * Timestamp of the last track image update.
     */
    public Long getLastImageUpdate() {
        return myLastImageUpdate;
    }

    public void setLastImageUpdate(Long lastImageUpdate) {
        myLastImageUpdate = lastImageUpdate;
    }

    /**
     * Media type of the track.
     */
    public MediaType getMediaType() {
        return myMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    /**
     * MP4 codec if the track is an MP4 file.
     */
    public String getMp4Codec() {
        return myMp4Codec;
    }

    public void setMp4Codec(String mp4Codec) {
        myMp4Codec = mp4Codec;
    }

    /**
     * Name of the track.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * Original artist name of the track.
     */
    public String getOriginalArtist() {
        return myOriginalArtist;
    }

    public void setOriginalArtist(String originalArtist) {
        myOriginalArtist = originalArtist;
    }

    /**
     * Number of times the track has been played.
     */
    public Long getPlayCount() {
        return myPlayCount;
    }

    public void setPlayCount(Long playCount) {
        myPlayCount = playCount;
    }

    /**
     * Disc number of the album of the track.
     */
    public Integer getDiscNumber() {
        return myDiscNumber;
    }

    public void setDiscNumber(Integer discNumber) {
        myDiscNumber = discNumber;
    }

    /**
     * Number of discs of the album of the track.
     */
    public Integer getDiscCount() {
        return myDiscCount;
    }

    public void setDiscCount(Integer discCount) {
        myDiscCount = discCount;
    }

    /**
     * TRUE if the track is DRM protected.
     */
    public Boolean isProtected() {
        return myProtected;
    }

    public void setProtected(Boolean aProtected) {
        myProtected = aProtected;
    }

    /**
     * Timestamp the track has been played the last time.
     */
    public Long getTsPlayed() {
        return myTsPlayed;
    }

    public void setTsPlayed(Long tsPlayed) {
        myTsPlayed = tsPlayed;
    }

    /**
     * Timestamp the track has been updated.
     */
    public Long getTsUpdated() {
        return myTsUpdated;
    }

    public void setTsUpdated(Long tsUpdated) {
        myTsUpdated = tsUpdated;
    }

    /**
     * Season if the track is a TV show episode.
     */
    public Integer getSeason() {
        return mySeason;
    }

    public void setSeason(Integer season) {
        mySeason = season;
    }

    /**
     * Name of the show if the track is a TV show episode.
     */
    public String getTvShow() {
        return myTvShow;
    }

    public void setTvShow(String tvShow) {
        myTvShow = tvShow;
    }

    /**
     * Source of the track.
     */
    public TrackSource getSource() {
        return mySource;
    }

    public void setSource(TrackSource source) {
        mySource = source;
    }

    /**
     * Length of the track in seconds.
     */
    public Integer getTime() {
        return myTime;
    }

    public void setTime(Integer time) {
        myTime = time;
    }

    /**
     * Track number.
     */
    public Integer getTrackNumber() {
        return myTrackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        myTrackNumber = trackNumber;
    }

    /**
     * Video type if the track is a video.
     */
    public VideoType getVideoType() {
        return myVideoType;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    /**
     * Year of the track.
     */
    public Integer getYear() {
        return myYear;
    }

    public void setYear(Integer year) {
        myYear = year;
    }
}