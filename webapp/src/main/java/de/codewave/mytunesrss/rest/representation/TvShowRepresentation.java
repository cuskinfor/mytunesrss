/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of a tv show.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TvShowRepresentation implements RestRepresentation, Comparable<TvShowRepresentation> {

    private URI mySeasonsUri;
    private URI myImageUri;
    private String myImageHash;
    private Integer mySeasonCount;
    private Integer myEpisodeCount;
    private String myName;

    public TvShowRepresentation() {
    }

    /**
     * URI of the seasons for this tv show.
     */
    public URI getSeasonsUri() {
        return mySeasonsUri;
    }

    public void setSeasonsUri(URI seasonsUri) {
        mySeasonsUri = seasonsUri;
    }

    /**
     * The URI of the TV show image.
     */
    public URI getImageUri() {
        return myImageUri;
    }

    public void setImageUri(URI imageUri) {
        myImageUri = imageUri;
    }

    /**
     * The unique hash of the TV show image.
     */
    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    /**
     * Number of seasons for this tv show.
     */
    public Integer getSeasonCount() {
        return mySeasonCount;
    }

    public void setSeasonCount(Integer seasonCount) {
        mySeasonCount = seasonCount;
    }

    /**
     * Number of episodes for this tv show.
     */
    public Integer getEpisodeCount() {
        return myEpisodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        myEpisodeCount = episodeCount;
    }

    /**
     * Name of this tv show.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int compareTo(TvShowRepresentation o) {
        return myName.compareTo(o.myName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TvShowRepresentation)) return false;

        TvShowRepresentation that = (TvShowRepresentation) o;

        if (!myName.equals(that.myName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return myName.hashCode();
    }
}
