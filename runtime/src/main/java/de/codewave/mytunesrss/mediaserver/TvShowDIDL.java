/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTvShowSeasonsQuery;
import de.codewave.mytunesrss.datastore.statement.TvShowSeason;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.container.PlaylistContainer;

import java.sql.SQLException;

public class TvShowDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, long firstResult, long maxResults) throws SQLException {
        final String series = decode(oidParams).get(0);
        executeAndProcess(
                tx,
                new FindTvShowSeasonsQuery(user, series),
                new DataStoreQuery.ResultProcessor<TvShowSeason>() {
                    @Override
                    public void process(TvShowSeason tvShowSeason) {
                        addContainer(new PlaylistContainer(ObjectID.TvShowSeason.getValue() + ";" + encode(series, Integer.toString(tvShowSeason.getNumber())), ObjectID.TvShow.getValue() + ";" + encode(series), getSeasonName(tvShowSeason.getNumber()), "MyTunesRSS", tvShowSeason.getEpisodeCount()));
                    }
                },
                firstResult,
                (int)maxResults
        );
    }

    private String getSeasonName(int season) {
        return "Season " + season; // TODO i18n?
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        FindTvShowSeasonsQuery seasonsQuery = new FindTvShowSeasonsQuery(user, decode(oidParams).get(0));
        int seasonsCount = tx.executeQuery(seasonsQuery).getResultSize();
        addContainer(createSimpleContainer(ObjectID.TvShow.getValue() + ";" + oidParams, ObjectID.TvShows.getValue(), seasonsCount));
        myTotalMatches = 1;
    }

}
