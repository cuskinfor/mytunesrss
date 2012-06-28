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
        return getName().compareTo(o.getName());
    }
}
