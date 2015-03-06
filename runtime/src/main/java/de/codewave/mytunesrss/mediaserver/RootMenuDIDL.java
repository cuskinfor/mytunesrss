/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RootMenuDIDL extends MyTunesRssContainerDIDL {

    private static final Logger LOGGER = LoggerFactory.getLogger(RootMenuDIDL.class);

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, long firstResult, long maxResults) throws SQLException {
        SystemInformation systemInformation = tx.executeQuery(new GetSystemInformationQuery());
        GetPlaylistCountQuery getPlaylistCountQuery = new GetPlaylistCountQuery(user, PlaylistFolderDIDL.PLAYLIST_TYPES, null, "ROOT", false, false);
        int playlistCount = tx.executeQuery(getPlaylistCountQuery);

        LOGGER.debug("Adding root menu containers.");

        List<Container> storageFolderList = new ArrayList<>();
        storageFolderList.add(new StorageFolder(ObjectID.PlaylistFolder.getValue(), ObjectID.Root.getValue(), "Playlists", "MyTunesRSS", playlistCount, 0L));
        if (user.isAudio()) {
            storageFolderList.add(new StorageFolder(ObjectID.Albums.getValue(), ObjectID.Root.getValue(), "Albums", "MyTunesRSS", systemInformation.getAlbumCount(), 0L));
            storageFolderList.add(new StorageFolder(ObjectID.Artists.getValue(), ObjectID.Root.getValue(), "Artists", "MyTunesRSS", systemInformation.getArtistCount(), 0L));
            storageFolderList.add(new StorageFolder(ObjectID.Genres.getValue(), ObjectID.Root.getValue(), "Genres", "MyTunesRSS", systemInformation.getGenreCount(), 0L));
        }
        if (user.isVideo()) {
            FindTvShowsQuery findTvShowsQuery = new FindTvShowsQuery(user);
            int tvShowCount = tx.executeQuery(findTvShowsQuery).getResultSize();
            storageFolderList.add(new StorageFolder(ObjectID.Movies.getValue(), ObjectID.Root.getValue(), "Movies", "MyTunesRSS", systemInformation.getMovieCount(), 0L));
            storageFolderList.add(new StorageFolder(ObjectID.TvShows.getValue(), ObjectID.Root.getValue(), "TV Shows", "MyTunesRSS", tvShowCount, 0L));
        }
        if (user.isPhotos() && !getClientProfile().getPhotoSizes().isEmpty()) {
            FindPhotoAlbumIdsQuery findPhotoAlbumIdsQuery = new FindPhotoAlbumIdsQuery();
            int photoAlbumCount = tx.executeQuery(findPhotoAlbumIdsQuery).size();
            List<Integer> photoSizes = getClientProfile().getPhotoSizes();
            if (photoSizes.size() > 0) {
                storageFolderList.add(
                        new StorageFolder(
                                photoSizes.size() > 1 ? ObjectID.PhotoAlbums.getValue() : ObjectID.PhotoAlbums.getValue() + ";" + encode(Integer.toString(photoSizes.get(0))),
                                ObjectID.Root.getValue(),
                                "Photos",
                                "MyTunesRSS",
                                photoSizes.size() > 1 ? photoSizes.size() : photoAlbumCount,
                                0L
                        )
                );
            }
        }
        for (Container storageFolder : MyTunesRssUtils.getSubList(storageFolderList, (int) firstResult, (int) maxResults)) {
            addContainer(storageFolder);
        }
        myTotalMatches = storageFolderList.size();
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        addContainer(createSimpleContainer("0", "", "MyTunesRSS", 7));
        myTotalMatches = 1;
    }

}
