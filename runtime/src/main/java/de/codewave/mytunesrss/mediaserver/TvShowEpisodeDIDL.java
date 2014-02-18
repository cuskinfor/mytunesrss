/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Movie;

public class TvShowEpisodeDIDL extends MyTunesRssItemDIDL {

    @Override
    protected Item createItem(Track track, User user) {
        return new Movie(
                ObjectID.TvShowEpisode.getValue() + ";" + encode(track.getId()),
                ObjectID.TvShowSeason.getValue() + ";" + encode(track.getSeries(), Integer.toString(track.getSeason())),
                track.getName(),
                "MyTunesRSS",
                createTrackResource(track, user));
    }

}
