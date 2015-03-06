package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindGenresQuery;
import de.codewave.mytunesrss.datastore.statement.Genre;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.container.MusicArtist;

import java.sql.SQLException;

public class GenresDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, long firstResult, long maxResults) throws SQLException {
        executeAndProcess(
                tx,
                new FindGenresQuery(user, false, -1),
                new DataStoreQuery.ResultProcessor<Genre>() {
                    @Override
                    public void process(Genre genre) {
                        addContainer(new MusicArtist(ObjectID.GenreAlbums.getValue() + ";" + encode(genre.getName()), ObjectID.Genres.getValue(), genre.getName(), "MyTunesRSS", genre.getAlbumCount()));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        addContainer(createSimpleContainer(ObjectID.Genres.getValue(), ObjectID.Root.getValue(), systemInformation.getGenreCount()));
        myTotalMatches = 1;
    }
    
}
