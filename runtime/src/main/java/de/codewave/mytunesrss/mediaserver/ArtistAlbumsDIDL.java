/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.Artist;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.FindArtistQuery;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;

public class ArtistAlbumsDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        final String artist = decode(oidParams).get(0);
        executeAndProcess(
                tx,
                new FindAlbumQuery(user, null, artist, false, null, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL),
                new DataStoreQuery.ResultProcessor<Album>() {
                    public void process(Album album) {
                        addContainer(createMusicAlbum(user, album, ObjectID.ArtistAlbum.getValue() + ";" + encode(album.getName(), album.getArtist()), ObjectID.ArtistAlbums.getValue() + ";" + artist));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        String artistName = decode(oidParams).get(0);
        Artist artist = tx.executeQuery(new FindArtistQuery(user, artistName, null, null, -1)).nextResult();
        addContainer(createSimpleContainer(ObjectID.ArtistAlbums.getValue() + ";" + oidParams, ObjectID.Artists.getValue(), artist.getAlbumCount()));
        myTotalMatches = 1;
    }

}
