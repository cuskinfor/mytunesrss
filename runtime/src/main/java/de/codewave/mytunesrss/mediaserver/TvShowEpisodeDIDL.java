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
    protected Item createItem(Track track, User user, String oidParams) {
        Movie movie = createMovieTrack(
                user,
                track,
                ObjectID.TvShowEpisode.getValue() + ";" + encode(track.getId()),
                ObjectID.TvShowSeason.getValue() + ";" + encode(track.getSeries(), Integer.toString(track.getSeason()))
        );
        // fix title by prepending the episode number
        movie.setTitle(track.getEpisode() + " - " + mapUnknown(track.getName()));
        return movie;
    }

}
