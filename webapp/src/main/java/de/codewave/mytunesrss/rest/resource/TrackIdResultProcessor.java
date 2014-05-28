/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;

import java.util.ArrayList;
import java.util.List;

public class TrackIdResultProcessor implements DataStoreQuery.ResultProcessor<Track> {

    private List<String> myTrackIds = new ArrayList<>();

    @Override
    public void process(Track track) {
        myTrackIds.add(track.getId());
    }

    public String[] getTrackIds() {
        return myTrackIds.toArray(new String[myTrackIds.size()]);
    }
}
