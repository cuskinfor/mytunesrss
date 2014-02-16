/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.SortCriterion;

import java.util.HashMap;
import java.util.Map;

public class MyTunesRssContentDirectoryService extends AbstractContentDirectoryService {

    private Map<ObjectID, Class<? extends MyTunesRssDIDLContent>> contentForOid = new HashMap<>();
    private Map<ObjectID, Class<? extends MyTunesRssDIDLContent>> contentForOidPrefix = new HashMap<>();

    public MyTunesRssContentDirectoryService() {
        // complete OIDs
        contentForOid.put(ObjectID.Playlists, PlaylistsDIDL.class);
        contentForOid.put(ObjectID.Albums, AlbumsDIDL.class);
        contentForOid.put(ObjectID.Artists, ArtistsDIDL.class);
        contentForOid.put(ObjectID.Genres, GenresDIDL.class);
        contentForOid.put(ObjectID.Movies, MoviesDIDL.class);
        contentForOid.put(ObjectID.TvShows, TvShowsDIDL.class);
        contentForOid.put(ObjectID.Photoalbums, PhotoAlbumsDIDL.class);
        // OID prefixes
        contentForOidPrefix.put(ObjectID.ArtistAlbums, ArtistAlbumsDIDL.class);
        contentForOidPrefix.put(ObjectID.GenreAlbums, GenreAlbumsDIDL.class);
        contentForOidPrefix.put(ObjectID.Album, AlbumDIDL.class);
    }

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws ContentDirectoryException {
        Class<? extends MyTunesRssDIDLContent> contentClass = contentForOid.get(ObjectID.fromValue(objectID));
        if (contentClass == null) {
            for (Map.Entry<ObjectID, Class<? extends MyTunesRssDIDLContent>> entry : contentForOidPrefix.entrySet()) {
                if (objectID.startsWith(entry.getKey().getValue())) {
                    contentClass = entry.getValue();
                    break;
                }
            }
            if (contentClass == null) {
                contentClass = RootMenuDIDL.class;
            }
        }
        try {
            MyTunesRssDIDLContent content = contentClass.newInstance();
            int separatorIndex = objectID.indexOf(';');
            String oidParams = separatorIndex > 0 && separatorIndex < objectID.length() - 1 ? objectID.substring(separatorIndex + 1) : null;
            if (browseFlag == BrowseFlag.DIRECT_CHILDREN) {
                content.initDirectChildren(oidParams, filter, firstResult, maxResults, orderby);
            } else if (browseFlag == BrowseFlag.METADATA) {
                content.initMetaData(oidParams);
            }
            return new BrowseResult(new DIDLParser().generate(content), content.getCount(), content.getTotalMatches());
        } catch (Exception e) {
            throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, "Could not create browse result: " + e.getMessage());
        }
    }

    @Override
    public BrowseResult search(String containerId, String searchCriteria, String filter, long firstResult, long maxResults, SortCriterion[] orderBy) throws ContentDirectoryException {
        return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
    }

}
