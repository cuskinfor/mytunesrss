/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Genre;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a genre.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GenreRepresentation implements RestRepresentation {

    /**
     * @exclude from swagger docs
     */
    private String myTracksUri;
    /**
     * @exclude from swagger docs
     */
    private String myAlbumsUri;
    /**
     * @exclude from swagger docs
     */
    private String myArtistsUri;
    /**
     * @exclude from swagger docs
     */
    private Integer myAlbumCount;
    /**
     * @exclude from swagger docs
     */
    private Integer myArtistCount;
    /**
     * @exclude from swagger docs
     */
    private Boolean myHidden;
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private String myNaturalSortName;
    /**
     * @exclude from swagger docs
     */
    private Integer myTrackCount;

    public GenreRepresentation() {
    }

    public GenreRepresentation(Genre genre) {
        if (IncludeExcludeInterceptor.isAttr("albumCount")) {
            setAlbumCount(genre.getAlbumCount());
        }
        if (IncludeExcludeInterceptor.isAttr("artistCount")) {
            setArtistCount(genre.getArtistCount());
        }
        if (IncludeExcludeInterceptor.isAttr("hidden")) {
            setHidden(genre.isHidden());
        }
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(genre.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("trackCount")) {
            setTrackCount(genre.getTrackCount());
        }
        if (IncludeExcludeInterceptor.isAttr("naturalSortName")) {
            setNaturalSortName(genre.getNaturalSortName());
        }
    }

    /**
     * URI for getting the tracks of this genre.
     */
    public String getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(String tracksUri) {
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

    /**
     * URI of the albums with tracks of this genre.
     */
    public String getAlbumsUri() {
        return myAlbumsUri;
    }

    public void setAlbumsUri(String albumsUri) {
        myAlbumsUri = albumsUri;
    }

    /**
     * URI of the artists with tracks of this genre.
     */
    public String getArtistsUri() {
        return myArtistsUri;
    }

    public void setArtistsUri(String artistsUri) {
        myArtistsUri = artistsUri;
    }

    /**
     * Natural sort name.
     */
    public String getNaturalSortName() {
        return myNaturalSortName;
    }

    public void setNaturalSortName(String naturalSortName) {
        myNaturalSortName = naturalSortName;
    }
}
