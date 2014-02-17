/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Movie;

public class MovieDIDL extends TrackDIDL {

    @Override
    protected Item createTrackItem(Track track, User user) {
        return new Movie(getObjectId(track), getParentId(track), track.getName(), "MyTunesRSS", createTrackResource(track, user));
    }

    @Override
    protected String getParentId(Track track) {
        return ObjectID.Movies.getValue();
    }

    @Override
    protected String getObjectId(Track track) {
        return ObjectID.Movie.getValue() + ";" + encode(track.getId());
    }
}
