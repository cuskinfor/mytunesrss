/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class RootMenuDIDL extends MyTunesRssDIDLContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootMenuDIDL.class);

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(user, null, null, null, false, false); // TODO: new query for count
        int playlistCount = tx.executeQuery(findPlaylistQuery).getResultSize();
        FindPhotoAlbumIdsQuery findPhotoAlbumIdsQuery = new FindPhotoAlbumIdsQuery(); // TODO: new query for count
        int photoAlbumCount = tx.executeQuery(findPhotoAlbumIdsQuery).size();

        LOGGER.debug("Adding root menu containers.");
        addContainer(new StorageFolder(ObjectID.Playlists.getValue(), "0", "Playlists", "MyTunesRSS", playlistCount, 0L));
        addContainer(new StorageFolder(ObjectID.Albums.getValue(), "0", "Albums", "MyTunesRSS", systemInformation.getAlbumCount(), 0L));
        addContainer(new StorageFolder(ObjectID.Artists.getValue(), "0", "Artists", "MyTunesRSS", systemInformation.getArtistCount(), 0L));
        addContainer(new StorageFolder(ObjectID.Genres.getValue(), "0", "Genres", "MyTunesRSS", systemInformation.getGenreCount(), 0L));
        addContainer(new StorageFolder(ObjectID.Movies.getValue(), "0", "Movies", "MyTunesRSS", systemInformation.getMovieCount(), 0L));
        addContainer(new StorageFolder(ObjectID.TvShows.getValue(), "0", "TV Shows", "MyTunesRSS", systemInformation.getTvShowCount(), 0L));
        addContainer(new StorageFolder(ObjectID.Photoalbums.getValue(), "0", "Photos", "MyTunesRSS", photoAlbumCount, 0L));
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {
        throw new NotYetImplementedException();
    }

    long getTotalMatches() {
        return getCount();
    }

}
