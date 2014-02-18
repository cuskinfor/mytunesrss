package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.MusicAlbum;

import java.net.URI;

public class AlbumsDIDL extends MyTunesRssDIDLContent {

    private long myTotalMatches;

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, final long maxResults, SortCriterion[] orderby) throws Exception {
        myTotalMatches = executeAndProcess(
                tx,
                new FindAlbumQuery(user, null, null, false, null, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL),
                new DataStoreQuery.ResultProcessor<Album>() {
                    public void process(Album album) {
                        MusicAlbum musicAlbum = new MusicAlbum(ObjectID.Album.getValue() + ";" + encode(album.getName(), album.getArtist()), ObjectID.Albums.getValue(), album.getName(), album.getArtist(), album.getTrackCount());
                        URI[] imageUris = getImageUris(user, 256, album.getImageHash());
                        if (imageUris.length > 0) {
                            musicAlbum.setAlbumArtURIs(imageUris);
                        }
                        addContainer(musicAlbum);
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {
        throw new NotYetImplementedException();
    }

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }

}
