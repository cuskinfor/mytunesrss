/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTvShowSeasonsQuery;
import de.codewave.mytunesrss.datastore.statement.TvShowSeason;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.PlaylistContainer;

import java.sql.SQLException;

public class TvShowDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        final String series = decode(oidParams).get(0);
        executeAndProcess(
                tx,
                new FindTvShowSeasonsQuery(user, series),
                new DataStoreQuery.ResultProcessor<TvShowSeason>() {
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

}
