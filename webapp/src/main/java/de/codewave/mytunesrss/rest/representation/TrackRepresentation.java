/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Track;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class TrackRepresentation extends Track {

    private Map<String, URI> myUri = new HashMap<String, URI>();

    public TrackRepresentation(Track track) {
        setId(track.getId());
        setAlbum(track.getAlbum());
        setAlbumArtist(track.getAlbumArtist());
        setArtist(track.getArtist());
        setComment(track.getComment());
        setComposer(track.getComposer());
        setEpisode(track.getEpisode());
        setFile(track.getFile());
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
        setSeries(track.getSeries());
        setSource(track.getSource());
        setTime(track.getTime());
        setTrackNumber(track.getTrackNumber());
        setVideoType(track.getVideoType());
        setYear(track.getYear());
    }

    public Map<String, URI> getUri() {
        return myUri;
    }
}
