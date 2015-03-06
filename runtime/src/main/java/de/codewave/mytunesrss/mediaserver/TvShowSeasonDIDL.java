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
import org.fourthline.cling.support.model.item.Movie;

import java.sql.SQLException;

public class TvShowSeasonDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, long firstResult, long maxResults) throws SQLException {
        executeAndProcess(
                tx,
                new FindTvShowEpisodesQuery(user, decode(oidParams).get(0), Integer.parseInt(decode(oidParams).get(1))),
                new DataStoreQuery.ResultProcessor<Track>() {
                    @Override
                    public void process(Track track) {
                        Movie movie = createMovieTrack(
                                user,
                                track,
                                ObjectID.TvShowEpisode.getValue() + ";" + encode(track.getId()),
                                ObjectID.TvShowSeason.getValue() + ";" + encode(track.getSeries(), Integer.toString(track.getSeason()))

                        );
                        // fix title by prepending the episode number
                        movie.setTitle(track.getEpisode() + " - " + mapUnknown(track.getName()));
                        addItem(movie);
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        FindTvShowEpisodesQuery episodesQuery = new FindTvShowEpisodesQuery(user, decode(oidParams).get(0), Integer.parseInt(decode(oidParams).get(1)));
        int episodesCount = tx.executeQuery(episodesQuery).getResultSize();
        addContainer(createSimpleContainer(ObjectID.TvShowSeason.getValue() + ";" + oidParams, ObjectID.TvShow.getValue() + ";" + encode(decode(oidParams).get(0)), episodesCount));
        myTotalMatches = 1;
    }

}
