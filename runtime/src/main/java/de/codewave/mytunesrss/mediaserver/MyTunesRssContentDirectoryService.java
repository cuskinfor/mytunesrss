/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.SortCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyTunesRssContentDirectoryService extends AbstractContentDirectoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssContentDirectoryService.class);

    private Map<String, Class<? extends MyTunesRssDIDL>> contentForOid = new HashMap<>();
    private Map<String, Class<? extends MyTunesRssDIDL>> contentForOidPrefix = new HashMap<>();

    public MyTunesRssContentDirectoryService() {
        // complete OIDs
        contentForOid.put(ObjectID.Root.getValue(), RootMenuDIDL.class);
        contentForOid.put(ObjectID.PlaylistFolder.getValue(), PlaylistFolderDIDL.class);
        contentForOid.put(ObjectID.Albums.getValue(), AlbumsDIDL.class);
        contentForOid.put(ObjectID.Artists.getValue(), ArtistsDIDL.class);
        contentForOid.put(ObjectID.Genres.getValue(), GenresDIDL.class);
        contentForOid.put(ObjectID.Movies.getValue(), MoviesDIDL.class);
        contentForOid.put(ObjectID.TvShows.getValue(), TvShowsDIDL.class);
        contentForOid.put(ObjectID.PhotoAlbums.getValue(), PhotoAlbumsDIDL.class);
        // OID prefixes
        contentForOidPrefix.put(ObjectID.ArtistAlbums.getValue(), ArtistAlbumsDIDL.class);
        contentForOidPrefix.put(ObjectID.ArtistAlbum.getValue(), ArtistAlbumDIDL.class);
        contentForOidPrefix.put(ObjectID.ArtistAlbumTrack.getValue(), ArtistAlbumTrackDIDL.class);
        contentForOidPrefix.put(ObjectID.GenreAlbums.getValue(), GenreAlbumsDIDL.class);
        contentForOidPrefix.put(ObjectID.GenreAlbum.getValue(), GenreAlbumDIDL.class);
        contentForOidPrefix.put(ObjectID.GenreAlbumTrack.getValue(), GenreAlbumTrackDIDL.class);
        contentForOidPrefix.put(ObjectID.Album.getValue(), AlbumDIDL.class);
        contentForOidPrefix.put(ObjectID.AlbumTrack.getValue(), AlbumTrackDIDL.class);
        contentForOidPrefix.put(ObjectID.Movie.getValue(), MovieDIDL.class);
        contentForOidPrefix.put(ObjectID.TvShow.getValue(), TvShowDIDL.class);
        contentForOidPrefix.put(ObjectID.TvShowSeason.getValue(), TvShowSeasonDIDL.class);
        contentForOidPrefix.put(ObjectID.TvShowEpisode.getValue(), TvShowEpisodeDIDL.class);
        contentForOidPrefix.put(ObjectID.PlaylistFolder.getValue(), PlaylistFolderDIDL.class);
        contentForOidPrefix.put(ObjectID.Playlist.getValue(), PlaylistDIDL.class);
        contentForOidPrefix.put(ObjectID.PlaylistTrack.getValue(), PlaylistTrackDIDL.class);
        contentForOidPrefix.put(ObjectID.PhotoAlbums.getValue(), PhotoAlbumsDIDL.class);
        contentForOidPrefix.put(ObjectID.PhotoAlbum.getValue(), PhotoAlbumDIDL.class);
        contentForOidPrefix.put(ObjectID.Photo.getValue(), PhotoDIDL.class);
    }

    @Override
    public void changeSystemUpdateID() {
        super.changeSystemUpdateID();
    }

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter, long firstResult, long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        LOGGER.debug("Received browse request [objectID=\"{}\", browseFlag=\"{}\", filter=\"{}\", firstResult={}, maxResults={}, orderBy=\"{}\"].", new Object[] {objectID, browseFlag, filter, firstResult, maxResults, orderBy});
        Class<? extends MyTunesRssDIDL> contentClass = contentForOid.get(objectID);
        if (contentClass == null) {
            for (Map.Entry<String, Class<? extends MyTunesRssDIDL>> entry : contentForOidPrefix.entrySet()) {
                if (objectID.startsWith(entry.getKey() + ";")) {
                    contentClass = entry.getValue();
                    break;
                }
            }
            if (contentClass == null) {
                contentClass = RootMenuDIDL.class;
            }
        }
        try {
            MyTunesRssDIDL content = contentClass.newInstance();
            int separatorIndex = objectID.indexOf(';');
            String oidParams = separatorIndex > 0 && separatorIndex < objectID.length() - 1 ? objectID.substring(separatorIndex + 1) : null;
            if (browseFlag == BrowseFlag.DIRECT_CHILDREN) {
                content.initDirectChildren(oidParams, filter, firstResult, maxResults, orderBy);
            } else if (browseFlag == BrowseFlag.METADATA) {
                content.initMetaData(oidParams, filter, firstResult, maxResults, orderBy);
            } else {
                LOGGER.warn("Unexpected browse flag \"" + browseFlag.name() + "\".");
                throw new ContentDirectoryException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unexpected browse flag \"" + browseFlag.name() + "\".");
            }
            try {
                return new BrowseResult(new DIDLParser().generate(content), content.getCount(), content.getTotalMatches());
            } catch (Exception e) {
                LOGGER.error("Could not create browse result.", e);
                throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, "Could not create browse result: " + e.getMessage());
            }
        } catch (RuntimeException | InstantiationException | IllegalAccessException | SQLException e) {
            LOGGER.error("Could not create browse result.", e);
            throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, "Could not create browse result: " + e.getMessage());
        }
    }

    @Override
    public BrowseResult search(String containerId, String searchCriteria, String filter, long firstResult, long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        LOGGER.debug("Received search request [containerID=\"{}\", searchCriteria=\"{}\", filter=\"{}\", firstResult={}, maxResults={}, orderBy=\"{}\"].", new Object[] {containerId, searchCriteria, filter, firstResult, maxResults, orderBy});
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }

}
