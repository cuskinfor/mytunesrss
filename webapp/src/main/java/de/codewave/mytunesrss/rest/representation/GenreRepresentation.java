/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Genre;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of a genre.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GenreRepresentation implements RestRepresentation {

    private URI myTracksUri;
    private Integer myAlbumCount;
    private Integer myArtistCount;
    private Boolean myHidden;
    private String myName;
    private Integer myTrackCount;

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
    public Integer getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(Integer albumCount) {
        myAlbumCount = albumCount;
    }

    /**
     * Number of artists which have tracks with the genre.
     */
    public Integer getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(Integer artistCount) {
        myArtistCount = artistCount;
    }

    /**
     * TRUE of this genre should be hidden from the interface or FALSE otherwise.
     */
    public Boolean isHidden() {
        return myHidden;
    }

    public void setHidden(Boolean hidden) {
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
    public Integer getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(Integer trackCount) {
        myTrackCount = trackCount;
    }
}
