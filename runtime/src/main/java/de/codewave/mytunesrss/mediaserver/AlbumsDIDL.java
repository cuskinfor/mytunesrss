package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlbumsDIDL extends MyTunesRssDIDLContent {

    private long myTotalMatches;

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        myTotalMatches = executeAndProcess(
                tx,
                new FindAlbumQuery(user, null, null, false, null, -1, 0, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL),
                new DataStoreQuery.ResultProcessor<Album>() {
                    public void process(Album album) {
                        addContainer(new MusicAlbum(ObjectID.Album.name()+ ";" + encode(album.getName(), album.getArtist()), ObjectID.Albums.name(), album.getName(), album.getArtist(), album.getTrackCount()));
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
