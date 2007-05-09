/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.lang.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.LoadPlaylistCommandHandler
 */
public abstract class LoadPlaylistCommandHandler extends MyTunesRssCommandHandler {
    protected void loadPlaylist() throws SQLException {
        String playlistId = getRequestParameter("playlist", null);
        if (StringUtils.isNotEmpty(playlistId)) {
            Playlist playlist = getDataStore().executeQuery(new FindPlaylistQuery(null, playlistId)).iterator().next();
            LinkedHashSet<Track> tracks = new LinkedHashSet<Track>(getDataStore().executeQuery(new FindPlaylistTracksQuery(playlistId, null)));
            getSession().setAttribute("playlist", playlist);
            getSession().setAttribute("playlistContent", tracks);
        } else {
            throw new IllegalArgumentException("Missing parameter!");
        }
    }
}