/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTvShowEpisodesQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.Movie;

import java.sql.SQLException;

public class TvShowSeasonDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {        final String show = decode(oidParams).get(0);
        final int season = Integer.parseInt(decode(oidParams).get(1));
        executeAndProcess(
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

}
