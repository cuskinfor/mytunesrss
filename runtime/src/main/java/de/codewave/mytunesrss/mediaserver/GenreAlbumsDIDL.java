/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.datastore.statement.FindGenreQuery;
import de.codewave.mytunesrss.datastore.statement.Genre;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

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
                        addContainer(createMusicAlbum(user, album, ObjectID.GenreAlbum.getValue() + ";" + encode(album.getName(), album.getArtist()), ObjectID.GenreAlbums.getValue() + ";" + oidParams));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        String genreName = decode(oidParams).get(0);
        Genre genre = tx.executeQuery(new FindGenreQuery(user, genreName));
        addContainer(createSimpleContainer(ObjectID.GenreAlbums.getValue() + ";" + oidParams, ObjectID.Genres.getValue(), genre.getAlbumCount()));
        myTotalMatches = 1;
    }

}
