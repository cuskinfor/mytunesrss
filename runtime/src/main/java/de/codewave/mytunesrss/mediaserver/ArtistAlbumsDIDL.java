/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

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

public class ArtistAlbumsDIDL extends MyTunesRssDIDLContent {

    private long myTotalMatches;

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        String artist = decode(oidParams).get(0);
        myTotalMatches = executeAndProcess(
                tx,
                new FindAlbumQuery(user, null, artist, false, null, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL),
                new DataStoreQuery.ResultProcessor<Album>() {
                    public void process(Album album) {
                        MusicAlbum musicAlbum = new MusicAlbum(ObjectID.ArtistAlbum.getValue() + ";" + encode(album.getName(), album.getArtist()), ObjectID.ArtistAlbums.getValue() + ";" + oidParams, album.getName(), album.getArtist(), album.getTrackCount());
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
