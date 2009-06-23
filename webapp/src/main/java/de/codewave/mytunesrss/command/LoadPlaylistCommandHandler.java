/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * de.codewave.mytunesrss.command.LoadPlaylistCommandHandler
 */
public abstract class LoadPlaylistCommandHandler extends MyTunesRssCommandHandler {
    protected void loadPlaylist() throws SQLException {
        String playlistId = getRequestParameter("playlist", null);
        if (StringUtils.isNotEmpty(playlistId)) {
            Playlist playlist = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, playlistId, null, false, false)).nextResult()
                    ;
            List<Track> tracks = new ArrayList<Track>(getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(),
                                                                                                                playlistId,
                                                                                                                null)).getResults());
            MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST, playlist);
            MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS, tracks);
        } else {
            throw new IllegalArgumentException("Missing parameter!");
        }
    }
}