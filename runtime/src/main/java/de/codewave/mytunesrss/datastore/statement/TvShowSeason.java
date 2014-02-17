/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

public class TvShowSeason {

    private int myNumber;
    private int myEpisodeCount;

    public int getNumber() {
        return myNumber;
    }

    public void setNumber(int number) {
        myNumber = number;
    }

    public int getEpisodeCount() {
        return myEpisodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        myEpisodeCount = episodeCount;
    }
}
