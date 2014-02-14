/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class RootMenuDIDL extends MyTunesRssDIDLContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootMenuDIDL.class);
    
    static final String ID_PLAYLISTS = "PL";
    static final String ID_ALBUMS = "AL";
    static final String ID_ARTISTS = "AR";
    static final String ID_GENRES = "GE";
    static final String ID_MOVIES = "MO";
    static final String ID_TVSHOWS = "TV";
    static final String ID_PHOTOS = "PH";

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String objectID, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(user, null, null, null, false, false); // TODO: new query for count
        int playlistCount = tx.executeQuery(findPlaylistQuery).getResultSize();
        FindPhotoAlbumIdsQuery findPhotoAlbumIdsQuery = new FindPhotoAlbumIdsQuery(); // TODO: new query for count
        int photoAlbumCount = tx.executeQuery(findPhotoAlbumIdsQuery).size();
        
        LOGGER.debug("Adding root menu containers.");
        addContainer(new StorageFolder(ID_PLAYLISTS, "0", "Playlists", "MyTunesRSS", playlistCount, 0L));
        addContainer(new StorageFolder(ID_ALBUMS, "0", "Albums", "MyTunesRSS", systemInformation.getAlbumCount(), 0L));
        addContainer(new StorageFolder(ID_ARTISTS, "0", "Artists", "MyTunesRSS", systemInformation.getArtistCount(), 0L));
        addContainer(new StorageFolder(ID_GENRES, "0", "Genres", "MyTunesRSS", systemInformation.getGenreCount(), 0L));
        addContainer(new StorageFolder(ID_MOVIES, "0", "Movies", "MyTunesRSS", systemInformation.getMovieCount(), 0L));
        addContainer(new StorageFolder(ID_TVSHOWS, "0", "TV Shows", "MyTunesRSS", systemInformation.getTvShowCount(), 0L));
        addContainer(new StorageFolder(ID_PHOTOS, "0", "Photos", "MyTunesRSS", photoAlbumCount, 0L));
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String objectID) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    long getTotalMatches() {
        LOGGER.debug("Root menu DIDL has " + getCount() + " total matches.");
        return getCount();
    }

}
