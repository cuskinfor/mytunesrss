/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.datastore.statement.Track;

public class TvShowEpisodeDIDL extends MovieDIDL {

    @Override
    protected String getParentId(Track track) {
        return ObjectID.TvShowSeason.getValue() + ";" + encode(track.getSeries(), Integer.toString(track.getSeason()));
    }

    @Override
    protected String getObjectId(Track track) {
        return ObjectID.TvShowEpisode.getValue() + ";" + encode(track.getId());
    }

}
