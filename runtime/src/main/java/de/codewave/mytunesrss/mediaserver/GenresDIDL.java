package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindGenreQuery;
import de.codewave.mytunesrss.datastore.statement.Genre;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.MusicArtist;

import java.sql.SQLException;

public class GenresDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        executeAndProcess(
                tx,
                new FindGenreQuery(user, false, -1),
                new DataStoreQuery.ResultProcessor<Genre>() {
                    public void process(Genre genre) {
                        addContainer(new MusicArtist(ObjectID.GenreAlbums.getValue() + ";" + encode(genre.getName()), ObjectID.Genres.getValue(), genre.getName(), "MyTunesRSS", genre.getAlbumCount()));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

}
