/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public class TvShow {

    private String myName;
    private int mySeasonCount;
    private int myEpisodeCount;

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getSeasonCount() {
        return mySeasonCount;
    }

    public void setSeasonCount(int seasonCount) {
        mySeasonCount = seasonCount;
    }

    public int getEpisodeCount() {
        return myEpisodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        myEpisodeCount = episodeCount;
    }
}
