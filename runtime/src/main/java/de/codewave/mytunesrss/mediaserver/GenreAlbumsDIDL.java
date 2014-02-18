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
import java.sql.SQLException;

public class GenreAlbumsDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        String genre = decode(oidParams).get(0);
        executeAndProcess(
                tx,
                new FindAlbumQuery(user, null, null, false, new String[] {genre}, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL),
                new DataStoreQuery.ResultProcessor<Album>() {
                    public void process(Album album) {
                        MusicAlbum musicAlbum = new MusicAlbum(ObjectID.GenreAlbum.getValue() + ";" + encode(album.getName(), album.getArtist()), ObjectID.GenreAlbums.getValue() + ";" + oidParams, album.getName(), album.getArtist(), album.getTrackCount());
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

}
