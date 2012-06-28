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
 * Representation of a tv show.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TvShowRepresentation implements RestRepresentation, Comparable<TvShowRepresentation> {

    private URI mySeasonsUri;
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
        return getName().compareTo(o.getName());
    }
}
