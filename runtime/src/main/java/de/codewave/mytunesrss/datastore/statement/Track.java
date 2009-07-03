/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * de.codewave.mytunesrss.datastore.statement.Track
 */
public class Track {
    private static final Logger LOGGER = LoggerFactory.getLogger(Track.class);

    private String myId;
    private String myName;
    private String myAlbum;
    private String myArtist;
    private String myOriginalArtist;
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
    private String filename;
    private TrackSource source;

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

    public String getOriginalArtist() {
        return myOriginalArtist;
    }

    public void setOriginalArtist(String originalArtist) {
        myOriginalArtist = originalArtist;
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

    public String getContentType() {
        String name = getFile().getName().toLowerCase();
        return FileSupportUtils.getContentType(name);
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public TrackSource getSource() {
        return source;
    }

    public void setSource(TrackSource source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object other) {
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