package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Artist;
import de.codewave.mytunesrss.datastore.statement.FindArtistQuery;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.MusicArtist;

import java.sql.SQLException;

public class ArtistsDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        executeAndProcess(
                tx,
                new FindArtistQuery(user, null, null, null, -1),
                new DataStoreQuery.ResultProcessor<Artist>() {
                    public void process(Artist artist) {
                        addContainer(new MusicArtist(ObjectID.ArtistAlbums.getValue() + ";" + encode(artist.getName()), ObjectID.Artists.getValue(), artist.getName(), "MyTunesRSS", artist.getAlbumCount()));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

}
