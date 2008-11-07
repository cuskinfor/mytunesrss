/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.LoadPlaylistCommandHandler
 */
public abstract class LoadPlaylistCommandHandler extends MyTunesRssCommandHandler {
    protected void loadPlaylist() throws SQLException {
        String playlistId = getRequestParameter("playlist", null);
        if (StringUtils.isNotEmpty(playlistId)) {
            Playlist playlist = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, playlistId, null, false, false)).nextResult();
            List<Track> tracks = new ArrayList<Track>(getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(),
                                                                                                                playlistId,
                                                                                                                null)).getResults());
            getSession().setAttribute("playlist", playlist);
            getSession().setAttribute("playlistContent", tracks);
        } else {
            throw new IllegalArgumentException("Missing parameter!");
        }
    }
}