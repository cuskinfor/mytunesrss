/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Genre;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a genre.
 */
@XmlRootElement
public class GenreRepresentation {

    private URI myTracksUri;
    private int myAlbumCount;
    private int myArtistCount;
    private boolean myHidden;
    private String myName;
    private int myTrackCount;

    public GenreRepresentation() {
    }

    public GenreRepresentation(Genre genre) {
        setAlbumCount(genre.getAlbumCount());
        setArtistCount(genre.getArtistCount());
        setHidden(genre.isHidden());
        setName(genre.getName());
        setTrackCount(genre.getTrackCount());
    }

    /**
     * URI for getting the tracks of this genre.
     */
    public URI getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(URI tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * Number of albums which contain tracks with this genre.
     */
    public int getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(int albumCount) {
        myAlbumCount = albumCount;
    }

    /**
     * Number of artists which have tracks with the genre.
     */
    public int getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(int artistCount) {
        myArtistCount = artistCount;
    }

    /**
     * TRUE of this genre should be hidden from the interface or FALSE otherwise.
     */
    public boolean isHidden() {
        return myHidden;
    }

    public void setHidden(boolean hidden) {
        myHidden = hidden;
    }

    /**
     * Name of the genre.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * Number of tracks with this genre.
     */
    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }
}
