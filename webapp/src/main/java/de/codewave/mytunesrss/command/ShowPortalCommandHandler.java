/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.OffHeapSessionStore;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.ShowPortalCommandHandler
 */
public class ShowPortalCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowPortalCommandHandler.class);

    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (isSessionAuthorized()) {
            String refreshSmartPlaylistId = getRequestParameter("refreshSmartPlaylistId", null);
            if (StringUtils.isNotBlank(refreshSmartPlaylistId)) {
                SmartPlaylist smartPlaylist = getTransaction().executeQuery(new FindSmartPlaylistQuery(refreshSmartPlaylistId));
                if (smartPlaylist != null) {
                    Collection<SmartInfo> smartInfos = smartPlaylist.getSmartInfos();
                    if (smartInfos != null) {
                        LOGGER.debug("Refreshing smart playlist \"" + smartPlaylist.getPlaylist().getName() + "\" with ID \"" + smartPlaylist.getPlaylist().getId() + "\".");
                        getTransaction().executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, refreshSmartPlaylistId));
                    }
                }
            }
            String containerId = getRequestParameter("cid", null);

            OffHeapSessionStore offHeapSessionStore = OffHeapSessionStore.get(getRequest());
            String currentListId = getRequestParameter(OffHeapSessionStore.CURRENT_LIST_ID, null);
            List<Playlist> cachedPlaylists = offHeapSessionStore.getCurrentList(currentListId);

            if (cachedPlaylists == null) {

                currentListId = offHeapSessionStore.newCurrentList();
                cachedPlaylists = offHeapSessionStore.getCurrentList(currentListId);

                if (StringUtils.isEmpty(containerId) && getAuthUser().isSpecialPlaylists()) {
                    cachedPlaylists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, PlaylistType.MyTunesSmart, getBundleString(
                            "playlist.specialAllByAlbum"), -1));
                    cachedPlaylists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ARTIST, PlaylistType.MyTunesSmart, getBundleString(
                            "playlist.specialAllByArtist"), -1));
                }
                if (StringUtils.isNotEmpty(containerId)) {
                    Playlist container = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, containerId, null, false, false))
                            .nextResult();
                    getRequest().setAttribute("container", container);
                }
                containerId = getRequestParameter("cid", "ROOT");
                FindPlaylistQuery query = new FindPlaylistQuery(getAuthUser(), null, null, containerId, false, false);
                query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
                getTransaction().executeQuery(query).addRemainingResults(cachedPlaylists);
            }

            getRequest().setAttribute(OffHeapSessionStore.CURRENT_LIST_ID, currentListId);

            List<Playlist> playlists;
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && cachedPlaylists.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(cachedPlaylists.size(), current);
                getRequest().setAttribute("pager", pager);
                playlists = MyTunesRssUtils.getSubList(cachedPlaylists, current * pageSize, pageSize);
            } else {
                playlists = cachedPlaylists;
            }

            getRequest().setAttribute("playlists", playlists);
            getRequest().setAttribute("uploadLink", getAuthUser().isUpload() && MyTunesRss.CONFIG.isUploadableDatasource());
            getRequest().setAttribute("statistics", getTransaction().executeQuery(new GetSystemInformationQuery()));
            getRequest().setAttribute("showRemoteControl", !MyTunesRss.VLC_PLAYER.getPlaylist().isEmpty());
            forward(MyTunesRssResource.Portal);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

}
