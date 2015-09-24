/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a tv show season.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TvShowSeasonRepresentation implements RestRepresentation, Comparable<TvShowSeasonRepresentation> {

    /**
     * @exclude from swagger docs
     */
    private String myEpisodesUri;
    /**
     * @exclude from swagger docs
     */
    private String myImageUri;
    /**
     * @exclude from swagger docs
     */
    private String myImageHash;
    /**
     * @exclude from swagger docs
     */
    private Integer myEpisodeCount;
    /**
     * @exclude from swagger docs
     */
    private Integer myName;

    /**
     * URI of the episodes for this tv show season.
     */
    public String getEpisodesUri() {
        return myEpisodesUri;
    }

    public void setEpisodesUri(String episodesUri) {
        myEpisodesUri = episodesUri;
    }

    /**
     * The URI of the TV show episode image.
     */
    public String getImageUri() {
        return myImageUri;
    }

    public void setImageUri(String imageUri) {
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

    @Override
    public int compareTo(TvShowSeasonRepresentation o) {
        return myName.compareTo(o.myName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TvShowSeasonRepresentation)) return false;

        TvShowSeasonRepresentation that = (TvShowSeasonRepresentation) o;

        return myName.equals(that.myName);

    }

    @Override
    public int hashCode() {
        return myName.hashCode();
    }
}
