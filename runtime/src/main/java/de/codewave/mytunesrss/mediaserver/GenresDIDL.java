package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.MusicArtist;

public class GenresDIDL extends MyTunesRssDIDLContent {

    private long myTotalMatches;

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        myTotalMatches = executeAndProcess(
                tx,
                new FindGenreQuery(user, false, -1),
                new DataStoreQuery.ResultProcessor<Genre>() {
                    public void process(Genre artist) {
                        addContainer(new MusicArtist(ObjectID.Genre.name() + ";" + encode(artist.getName()), ObjectID.Genres.name(), artist.getName(), "MyTunesRSS", artist.getAlbumCount()));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }
}
