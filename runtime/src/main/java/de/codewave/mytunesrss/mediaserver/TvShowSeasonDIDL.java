/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.FindTvShowEpisodesQuery;
import de.codewave.mytunesrss.datastore.statement.FindTvShowSeasonsQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.Movie;

public class TvShowSeasonDIDL extends MyTunesRssDIDLContent {

    private long myTotalMatches;

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        final String show = decode(oidParams).get(0);
        final int season = Integer.parseInt(decode(oidParams).get(1));
        myTotalMatches = executeAndProcess(
                tx,
                new FindTvShowEpisodesQuery(user, show, season),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        addItem(new Movie(ObjectID.TvShowEpisode.getValue() + ";" + encode(track.getId()), ObjectID.TvShowSeason.getValue() + ";" + encode(track.getSeries(), Integer.toString(track.getSeason())), track.getName(), "MyTunesRSS", createTrackResource(track, user)));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {

    }

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }
}
