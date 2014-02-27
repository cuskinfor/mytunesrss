package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;

public class AlbumsDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, final long maxResults, SortCriterion[] orderby) throws SQLException {
        executeAndProcess(
                tx,
                new FindAlbumQuery(user, null, null, false, null, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL),
                new DataStoreQuery.ResultProcessor<Album>() {
                    public void process(Album album) {
                        addContainer(createMusicAlbum(user, album, ObjectID.Album.getValue() + ";" + encode(album.getName(), album.getArtist()), ObjectID.Albums.getValue()));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        addContainer(createSimpleContainer(ObjectID.Albums.getValue(), ObjectID.Root.getValue(), systemInformation.getAlbumCount()));
        myTotalMatches = 1;
    }
}
