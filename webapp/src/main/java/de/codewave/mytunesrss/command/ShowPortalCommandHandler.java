/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
            List<Playlist> playlists = new ArrayList<Playlist>();
            if (StringUtils.isEmpty(containerId) && getAuthUser().isSpecialPlaylists()) {
                playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, PlaylistType.MyTunesSmart, getBundleString(
                        "playlist.specialAllByAlbum"), -1));
                playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ARTIST, PlaylistType.MyTunesSmart, getBundleString(
                        "playlist.specialAllByArtist"), -1));
            }
            if (StringUtils.isNotEmpty(containerId)) {
                Playlist container = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, containerId, null, false, false))
                        .nextResult();
                getRequest().setAttribute("container", container);
            }
            containerId = getRequestParameter("cid", "ROOT");
            DataStoreQuery.QueryResult<Playlist> queryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                   null,
                                                                                                                   null,
                                                                                                                   containerId,
                                                                                                                   false,
                                                                                                                   false));
            for (Playlist playlist = queryResult.nextResult(); playlist != null; playlist = queryResult.nextResult()) {
                playlists.add(playlist);
                playlists.addAll(createSplittedPlaylists(playlist));
            }
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && playlists.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(playlists.size(), current);
                getRequest().setAttribute("pager", pager);
                playlists = MyTunesRssUtils.getSubList(playlists, current * pageSize, pageSize);
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

    private List<Playlist> createSplittedPlaylists(Playlist playlist) {
        List<Playlist> splittedPlaylists = new ArrayList<Playlist>();
        int maxCount = getWebConfig().getRssFeedLimit();
        if (maxCount > 0 && playlist.getTrackCount() > maxCount) {
            int startIndex = 0;
            while (startIndex < playlist.getTrackCount()) {
                int endIndex = Math.min(startIndex + maxCount - 1, playlist.getTrackCount() - 1);
                Playlist splittedPlaylist = new Playlist();
                splittedPlaylist.setType(playlist.getType());
                splittedPlaylist.setId(playlist.getId() + "@" + startIndex + "@" + endIndex);
                splittedPlaylist.setName(playlist.getName() + " [" + createFixedLengthNumber(startIndex + 1, playlist.getTrackCount()) + "-" +
                        createFixedLengthNumber(endIndex + 1, playlist.getTrackCount()) + "]");
                splittedPlaylist.setTrackCount(endIndex - startIndex + 1);
                splittedPlaylists.add(splittedPlaylist);
                startIndex = endIndex + 1;
            }
        }
        return splittedPlaylists;
    }

    private String createFixedLengthNumber(int number, int maxNumber) {
        String fixedLengthNumber = Integer.toString(number);
        int length = Integer.toString(maxNumber).length();
        while (fixedLengthNumber.length() < length) {
            fixedLengthNumber = "0" + fixedLengthNumber;
        }
        return fixedLengthNumber;
    }
}
