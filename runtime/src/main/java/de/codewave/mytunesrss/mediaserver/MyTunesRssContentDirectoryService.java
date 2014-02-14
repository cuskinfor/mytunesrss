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

    private Map<String, Class<? extends MyTunesRssDIDLContent>> contentForOid = new HashMap<>();
    private Map<String, Class<? extends MyTunesRssDIDLContent>> contentForOidPrefix = new HashMap<>();

    public MyTunesRssContentDirectoryService() {
        // complete OIDs
        contentForOid.put(RootMenuDIDL.ID_PLAYLISTS, PlaylistsDIDL.class);
        contentForOid.put(RootMenuDIDL.ID_ALBUMS, AlbumsDIDL.class);
        contentForOid.put(RootMenuDIDL.ID_ARTISTS, ArtistsDIDL.class);
        contentForOid.put(RootMenuDIDL.ID_GENRES, GenresDIDL.class);
        contentForOid.put(RootMenuDIDL.ID_MOVIES, MoviesDIDL.class);
        contentForOid.put(RootMenuDIDL.ID_TVSHOWS, TvShowsDIDL.class);
        contentForOid.put(RootMenuDIDL.ID_PHOTOS, PhotoAlbumsDIDL.class);
        // OID prefixes
    }

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws ContentDirectoryException {
        Class<? extends MyTunesRssDIDLContent> contentClass = contentForOid.get(objectID);
        if (contentClass == null) {
            for (Map.Entry<String, Class<? extends MyTunesRssDIDLContent>> entry : contentForOidPrefix.entrySet()) {
                if (objectID.startsWith(entry.getKey())) {
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
            if (browseFlag == BrowseFlag.DIRECT_CHILDREN) {
                content.initDirectChildren(objectID, filter, firstResult, maxResults, orderby);
            } else if (browseFlag == BrowseFlag.METADATA) {
                content.initMetaData(objectID);
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
