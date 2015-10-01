/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a track.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TrackRepresentation implements RestRepresentation {

    /**
     * @exclude from swagger docs
     */
    private String myImageUri;
    /**
     * @exclude from swagger docs
     */
    private String myM3uUri;
    /**
     * @exclude from swagger docs
     */
    private String myXspfUri;
    /**
     * @exclude from swagger docs
     */
    private String myRssUri;
    /**
     * @exclude from swagger docs
     */
    private String myDownloadUri;
    /**
     * @exclude from swagger docs
     */
    private String myPlaybackUri;
    /**
     * @exclude from swagger docs
     */
    private String myHttpLiveStreamUri;
    /**
     * @exclude from swagger docs
     */
    private String myArtistUri;
    /**
     * @exclude from swagger docs
     */
    private String myAlbumUri;
    /**
     * @exclude from swagger docs
     */
    private String myId;
    /**
     * @exclude from swagger docs
     */
    private String myAlbum;
    /**
     * @exclude from swagger docs
     */
    private String myAlbumArtist;
    /**
     * @exclude from swagger docs
     */
    private String myArtist;
    /**
     * @exclude from swagger docs
     */
    private String myComment;
    /**
     * @exclude from swagger docs
     */
    private String myComposer;
    /**
     * @exclude from swagger docs
     */
    private Integer myEpisode;
    /**
     * @exclude from swagger docs
     */
    private String myFilename;
    /**
     * @exclude from swagger docs
     */
    private String myGenre;
    /**
     * @exclude from swagger docs
     */
    private String myImageHash;
    /**
     * @exclude from swagger docs
     */
    private Long myLastImageUpdate;
    /**
     * @exclude from swagger docs
     */
    private MediaType myMediaType;
    /**
     * @exclude from swagger docs
     */
    private String myMp4Codec;
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private Long myPlayCount;
    /**
     * @exclude from swagger docs
     */
    private Integer myDiscNumber;
    /**
     * @exclude from swagger docs
     */
    private Integer myDiscCount;
    /**
     * @exclude from swagger docs
     */
    private Boolean myProtected;
    /**
     * @exclude from swagger docs
     */
    private Long myTsPlayed;
    /**
     * @exclude from swagger docs
     */
    private Long myTsUpdated;
    /**
     * @exclude from swagger docs
     */
    private Integer mySeason;
    /**
     * @exclude from swagger docs
     */
    private String myTvShow;
    /**
     * @exclude from swagger docs
     */
    private TrackSource mySource;
    /**
     * @exclude from swagger docs
     */
    private Integer myTime;
    /**
     * @exclude from swagger docs
     */
    private Integer myTrackNumber;
    /**
     * @exclude from swagger docs
     */
    private VideoType myVideoType;
    /**
     * @exclude from swagger docs
     */
    private Integer myYear;

    public TrackRepresentation() {
    }

    public TrackRepresentation(Track track) {
        if (IncludeExcludeInterceptor.isAttr("id")) {
            setId(track.getId());
        }
        if (IncludeExcludeInterceptor.isAttr("album")) {
            setAlbum(track.getAlbum());
        }
        if (IncludeExcludeInterceptor.isAttr("albumArtist")) {
            setAlbumArtist(track.getAlbumArtist());
        }
        if (IncludeExcludeInterceptor.isAttr("artist")) {
            setArtist(track.getArtist());
        }
        if (IncludeExcludeInterceptor.isAttr("comment")) {
            setComment(track.getComment());
        }
        if (IncludeExcludeInterceptor.isAttr("composer")) {
            setComposer(track.getComposer());
        }
        if (IncludeExcludeInterceptor.isAttr("episode")) {
            setEpisode(track.getEpisode());
        }
        if (IncludeExcludeInterceptor.isAttr("filename")) {
            setFilename(track.getFilename());
        }
        if (IncludeExcludeInterceptor.isAttr("genre")) {
            setGenre(track.getGenre());
        }
        if (IncludeExcludeInterceptor.isAttr("imageHash")) {
            setImageHash(StringUtils.trimToNull(track.getImageHash()));
        }
        if (IncludeExcludeInterceptor.isAttr("lastImageUpdate")) {
            setLastImageUpdate(track.getLastImageUpdate());
        }
        if (IncludeExcludeInterceptor.isAttr("mediaType")) {
            setMediaType(track.getMediaType());
        }
        if (IncludeExcludeInterceptor.isAttr("mp4Codec")) {
            setMp4Codec(track.getMp4Codec());
        }
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(track.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("playCount")) {
            setPlayCount(track.getPlayCount());
        }
        if (IncludeExcludeInterceptor.isAttr("discNumber")) {
            setDiscNumber(track.getPosNumber());
        }
        if (IncludeExcludeInterceptor.isAttr("discCount")) {
            setDiscCount(track.getPosSize());
        }
        if (IncludeExcludeInterceptor.isAttr("protected")) {
            setProtected(track.isDrmProtected());
        }
        if (IncludeExcludeInterceptor.isAttr("tsPlayed")) {
            setTsPlayed(track.getTsPlayed());
        }
        if (IncludeExcludeInterceptor.isAttr("tsUpdated")) {
            setTsUpdated(track.getTsUpdated());
        }
        if (IncludeExcludeInterceptor.isAttr("season")) {
            setSeason(track.getSeason());
        }
        if (IncludeExcludeInterceptor.isAttr("tvShow")) {
            setTvShow(track.getSeries());
        }
        if (IncludeExcludeInterceptor.isAttr("source")) {
            setSource(track.getSource());
        }
        if (IncludeExcludeInterceptor.isAttr("time")) {
            setTime(track.getTime());
        }
        if (IncludeExcludeInterceptor.isAttr("trackNumber")) {
            setTrackNumber(track.getTrackNumber());
        }
        if (IncludeExcludeInterceptor.isAttr("videoType")) {
            setVideoType(track.getVideoType());
        }
        if (IncludeExcludeInterceptor.isAttr("year")) {
            setYear(track.getYear());
        }
    }

    /**
     * URI to the image of the track.
     */
    public String getImageUri() {
        return myImageUri;
    }

    public void setImageUri(String imageUri) {
        myImageUri = imageUri;
    }

    /**
     * URI to an M3U playlist of the track.
     */
    public String getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(String m3uUri) {
        myM3uUri = m3uUri;
    }

    /**
     * URI to an XSPF playlist of the track.
     */
    public String getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(String xspfUri) {
        myXspfUri = xspfUri;
    }

    /**
     * URI to an RSS feed of the track.
     */
    public String getRssUri() {
        return myRssUri;
    }

    public void setRssUri(String rssUri) {
        myRssUri = rssUri;
    }

    /**
     * Download URI of the track.
     */
    public String getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        myDownloadUri = downloadUri;
    }

    /**
     * Playback URI of the track.
     */
    public String getPlaybackUri() {
        return myPlaybackUri;
    }

    public void setPlaybackUri(String playbackUri) {
        myPlaybackUri = playbackUri;
    }

    /**
     * HTTP Live Streaming URI of the track.
     */
    public String getHttpLiveStreamUri() {
        return myHttpLiveStreamUri;
    }

    public void setHttpLiveStreamUri(String httpLiveStreamUri) {
        myHttpLiveStreamUri = httpLiveStreamUri;
    }

    /**
     * URI to the artist of the track.
     */
    public String getArtistUri() {
        return myArtistUri;
    }

    public void setArtistUri(String artistUri) {
        myArtistUri = artistUri;
    }

    /**
     * URI to the album of the track.
     */
    public String getAlbumUri() {
        return myAlbumUri;
    }

    public void setAlbumUri(String albumUri) {
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
