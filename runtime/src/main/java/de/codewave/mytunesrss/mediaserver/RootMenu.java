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
import org.fourthline.cling.support.model.container.StorageFolder;

import java.sql.SQLException;

public class RootMenu extends MyTunesRssDIDLContent {

    static final String ID_PLAYLISTS = "#PL";
    static final String ID_ALBUMS = "#AL";
    static final String ID_ARTISTS = "#AR";
    static final String ID_GENRES = "#GE";
    static final String ID_MOVIES = "#MO";
    static final String ID_TVSHOWS = "#TV";
    static final String ID_PHOTOS = "#PH";

    void create(User user, DataStoreSession tx) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(user, null, null, null, false, false); // TODO: new query for count
        findPlaylistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1);
        int playlistCount = tx.executeQuery(findPlaylistQuery).getResultSize();
        FindPhotoAlbumIdsQuery findPhotoAlbumIdsQuery = new FindPhotoAlbumIdsQuery(); // TODO: new query for count
        findPhotoAlbumIdsQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1);
        int photoAlbumCount = tx.executeQuery(findPhotoAlbumIdsQuery).size();

        addContainer(new StorageFolder(ID_PLAYLISTS, "0", "Playlists", "MyTunesRSS", playlistCount, 0L));
        addContainer(new StorageFolder(ID_ALBUMS, "0", "Albums", "MyTunesRSS", systemInformation.getAlbumCount(), 0L));
        addContainer(new StorageFolder(ID_ARTISTS, "0", "Artists", "MyTunesRSS", systemInformation.getArtistCount(), 0L));
        addContainer(new StorageFolder(ID_GENRES, "0", "Genres", "MyTunesRSS", systemInformation.getGenreCount(), 0L));
        addContainer(new StorageFolder(ID_MOVIES, "0", "Movies", "MyTunesRSS", systemInformation.getMovieCount(), 0L));
        addContainer(new StorageFolder(ID_TVSHOWS, "0", "TV Shows", "MyTunesRSS", systemInformation.getTvShowCount(), 0L));
        addContainer(new StorageFolder(ID_PHOTOS, "0", "Photos", "MyTunesRSS", photoAlbumCount, 0L));
    }

    long getTotalMatches() {
        return getCount();
    }

}
