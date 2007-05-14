/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.ShowPortalCommandHandler
 */
public class ShowPortalCommandHandler extends MyTunesRssCommandHandler {

    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (isSessionAuthorized()) {
            List<Playlist> playlists = new ArrayList<Playlist>();
            if (getAuthUser().isSpecialPlaylists()) {
                playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM,
                                           PlaylistType.MyTunes,
                                           getBundleString("playlist.specialAllByAlbum"),
                                           -1));
                playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ARTIST, PlaylistType.MyTunes, getBundleString(
                        "playlist.specialAllByArtist"), -1));
                int randomPlaylistSize = getWebConfig().getRandomPlaylistSize();
                if (randomPlaylistSize > 0) {
                    playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_RANDOM + "_" + randomPlaylistSize,
                                               PlaylistType.MyTunes,
                                               MessageFormat.format(getBundleString("playlist.specialRandom"), randomPlaylistSize),
                                               -1));
                }
            }
            for (Playlist playlist : getDataStore().executeQuery(new FindPlaylistQuery(null, null))) {
                playlists.add(playlist);
                playlists.addAll(createSplittedPlaylists(playlist));
            }
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && playlists.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(playlists.size(), current);
                getRequest().setAttribute("pager", pager);
                playlists = playlists.subList(current * pageSize, Math.min((current * pageSize) + pageSize, playlists.size()));
            }
            getRequest().setAttribute("playlists", playlists);
            getRequest().setAttribute("uploadLink", getAuthUser().isUpload() && StringUtils.isNotEmpty(MyTunesRss.CONFIG.getUploadDir()));
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
                splittedPlaylist.setId(playlist.getId() + "_" + startIndex + "_" + endIndex);
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