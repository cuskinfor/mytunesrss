/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieDIDL extends MyTunesRssItemDIDL {

    @Override
    protected Item createItem(Track track, User user) {
        return createMovieTrack(
                user,
                track,
                ObjectID.Movie.getValue() + ";" + encode(track.getId()),
                ObjectID.Movies.getValue()
        );
    }

}
