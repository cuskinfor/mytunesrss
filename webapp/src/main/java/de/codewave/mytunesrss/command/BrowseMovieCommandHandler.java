/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;

import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseMovieCommandHandler extends BrowseVideoCommandHandler {
    @Override
    protected List<Track> getEnhancedTracks(List<Track> tracks) {
        return tracks;
    }

    @Override
    protected DataStoreQuery<DataStoreQuery.QueryResult<Track>> getQuery() {
        return FindTrackQuery.getMovies(getAuthUser());
    }

    @Override
    protected MyTunesRssResource getResource(List<? extends Track> tracks) {
        return MyTunesRssResource.BrowseMovie;
    }
}