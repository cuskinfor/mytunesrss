/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTvShowEpisodesQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;

public class TvShowSeasonDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        executeAndProcess(
                tx,
                new FindTvShowEpisodesQuery(user, decode(oidParams).get(0), Integer.parseInt(decode(oidParams).get(1))),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        addItem(createMovieTrack(
                                user,
                                track,
                                ObjectID.TvShowEpisode.getValue() + ";" + encode(track.getId()),
                                ObjectID.TvShowSeason.getValue() + ";" + encode(track.getSeries(), Integer.toString(track.getSeason()))

                        ));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        FindTvShowEpisodesQuery episodesQuery = new FindTvShowEpisodesQuery(user, decode(oidParams).get(0), Integer.parseInt(decode(oidParams).get(1)));
        int episodesCount = tx.executeQuery(episodesQuery).getResultSize();
        addContainer(createSimpleContainer(ObjectID.TvShowSeason.getValue() + ";" + oidParams, ObjectID.TvShow.getValue() + ";" + encode(decode(oidParams).get(0)), episodesCount));
        myTotalMatches = 1;
    }

}
