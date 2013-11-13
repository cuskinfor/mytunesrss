/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of a tv show season.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TvShowSeasonRepresentation implements RestRepresentation, Comparable<TvShowSeasonRepresentation> {

    private URI myEpisodesUri;
    private URI myImageUri;
    private String myImageHash;
    private Integer myEpisodeCount;
    private Integer myName;

    public TvShowSeasonRepresentation() {
    }

    /**
     * URI of the episodes for this tv show season.
     */
    public URI getEpisodesUri() {
        return myEpisodesUri;
    }

    public void setEpisodesUri(URI episodesUri) {
        myEpisodesUri = episodesUri;
    }

    /**
     * The URI of the TV show episode image.
     */
    public URI getImageUri() {
        return myImageUri;
    }

    public void setImageUri(URI imageUri) {
        myImageUri = imageUri;
    }

    /**
     * The unique hash of the TV show episode image.
     */
    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    /**
     * Number of episodes for this tv show season.
     */
    public Integer getEpisodeCount() {
        return myEpisodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        myEpisodeCount = episodeCount;
    }

    /**
     * Name of this tv show season.
     */
    public Integer getName() {
        return myName;
    }

    public void setName(Integer name) {
        myName = name;
    }

    public int compareTo(TvShowSeasonRepresentation o) {
        return myName.compareTo(o.myName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TvShowSeasonRepresentation)) return false;

        TvShowSeasonRepresentation that = (TvShowSeasonRepresentation) o;

        if (!myName.equals(that.myName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return myName.hashCode();
    }
}
