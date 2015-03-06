package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTvShowsQuery;
import de.codewave.mytunesrss.datastore.statement.TvShow;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.container.PlaylistContainer;

import java.sql.SQLException;

public class TvShowsDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, long firstResult, long maxResults) throws SQLException {
        executeAndProcess(
                tx,
                new FindTvShowsQuery(user),
                new DataStoreQuery.ResultProcessor<TvShow>() {
                    @Override
                    public void process(TvShow tvShow) {
                        addContainer(new PlaylistContainer(ObjectID.TvShow.getValue() + ";" + encode(tvShow.getName()), ObjectID.TvShows.getValue(), mapUnknown(tvShow.getName()), "MyTunesRSS", tvShow.getSeasonCount()));
                    }
                },
                firstResult,
                (int)maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        FindTvShowsQuery findTvShowsQuery = new FindTvShowsQuery(user);
        int tvShowCount = tx.executeQuery(findTvShowsQuery).getResultSize();
        addContainer(createSimpleContainer(ObjectID.TvShows.getValue(), ObjectID.Root.getValue(), tvShowCount));
        myTotalMatches = 1;
    }

}
