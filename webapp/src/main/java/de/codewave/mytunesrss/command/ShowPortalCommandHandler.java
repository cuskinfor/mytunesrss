/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.ShowPortalCommandHandler
 */
public class ShowPortalCommandHandler extends MyTunesRssCommandHandler {

    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (isSessionAuthorized()) {
            String containerId = getRequestParameter("cid", null);
            List<Playlist> playlists = new ArrayList<Playlist>();
            if (StringUtils.isEmpty(containerId) && getAuthUser().isSpecialPlaylists()) {
                playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, PlaylistType.MyTunes, getBundleString(
                        "playlist.specialAllByAlbum"), -1));
                playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ARTIST, PlaylistType.MyTunes, getBundleString(
                        "playlist.specialAllByArtist"), -1));
                int mostPlayedPlaylistSize = getWebConfig().getMostPlayedPlaylistSize();
                if (mostPlayedPlaylistSize > 0) {
                    playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_MOST_PLAYED + "_" + mostPlayedPlaylistSize,
                                               PlaylistType.MyTunes,
                                               MessageFormat.format(getBundleString("playlist.specialMostPlayed"), mostPlayedPlaylistSize),
                                               mostPlayedPlaylistSize));
                }
                int lastUpdatedPlaylistSize = getWebConfig().getLastUpdatedPlaylistSize();
                if (lastUpdatedPlaylistSize > 0) {
                    playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_LAST_UPDATED + "_" + lastUpdatedPlaylistSize,
                                               PlaylistType.MyTunes,
                                               MessageFormat.format(getBundleString("playlist.specialLastUpdated"), lastUpdatedPlaylistSize),
                                               lastUpdatedPlaylistSize));
                }
                int randomPlaylistSize = getWebConfig().getRandomPlaylistSize();
                if (randomPlaylistSize > 0) {
                    DataStoreQuery.QueryResult<Playlist> randomPlaylistSources = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                                     null,
                                                                                                                                     getWebConfig().getRandomSource(),
                                                                                                                                     null,
                                                                                                                                     false,
                                                                                                                                     false));
                    StringBuilder randomType = new StringBuilder();
                    if (getWebConfig().isRandomAudio()) {
                        randomType.append('a');
                    }
                    if (getWebConfig().isRandomVideo()) {
                        randomType.append('v');
                    }
                    if (getWebConfig().isRandomProtected()) {
                        randomType.append('p');
                    }
                    if (randomPlaylistSources.getResultSize() == 1) {
                        Playlist firstResult = randomPlaylistSources.getResult(0);
                        playlists.add(new Playlist(
                                FindPlaylistTracksQuery.PSEUDO_ID_RANDOM + "_" + randomType.toString() + "_" + randomPlaylistSize + "_" +
                                        firstResult.getId(), PlaylistType.MyTunes, MessageFormat.format(getBundleString("playlist.specialRandom"),
                                                                                                        randomPlaylistSize,
                                                                                                        firstResult.getName()), randomPlaylistSize));
                    } else {
                        playlists.add(new Playlist(FindPlaylistTracksQuery.PSEUDO_ID_RANDOM + "_" + randomType.toString() + "_" + randomPlaylistSize,
                                                   PlaylistType.MyTunes,
                                                   MessageFormat.format(getBundleString("playlist.specialRandomWholeLibrary"), randomPlaylistSize),
                                                   randomPlaylistSize));
                    }
                }
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
                playlists = playlists.subList(current * pageSize, Math.min((current * pageSize) + pageSize, playlists.size()));
            }
            getRequest().setAttribute("playlists", playlists);
            getRequest().setAttribute("uploadLink", getAuthUser().isUpload() && StringUtils.isNotEmpty(MyTunesRss.CONFIG.getUploadDir()));
            getRequest().setAttribute("statistics", getTransaction().executeQuery(new GetSystemInformationQuery()));
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