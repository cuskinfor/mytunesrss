package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;

import java.sql.SQLException;

public class MoviesDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, String oidParams, long firstResult, long maxResults) throws SQLException {
        executeAndProcess(
                tx,
                FindTrackQuery.getMovies(user),
                new DataStoreQuery.ResultProcessor<Track>() {
                    @Override
                    public void process(Track track) {
                        addItem(createMovieTrack(
                                user,
                                track,
                                ObjectID.Movie.getValue() + ";" + encode(track.getId()),
                                ObjectID.Movies.getValue()
                        ));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        addContainer(createSimpleContainer(ObjectID.Movies.getValue(), ObjectID.Root.getValue(), systemInformation.getMovieCount()));
        myTotalMatches = 1;
    }

}
