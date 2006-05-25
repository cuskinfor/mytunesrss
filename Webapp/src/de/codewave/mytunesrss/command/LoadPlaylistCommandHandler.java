/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.command.LoadPlaylistCommandHandler
 */
public abstract class LoadPlaylistCommandHandler extends MyTunesRssCommandHandler {
    protected void loadPlaylist() throws SQLException {
        String playlistId = getRequestParameter("playlist", null);
        Playlist playlist = getDataStore().executeQuery(new FindPlaylistQuery(playlistId)).iterator().next();
        LinkedHashSet<Track> tracks = new LinkedHashSet<Track>(getDataStore().executeQuery(new FindPlaylistTracksQuery(playlistId)));
        getSession().setAttribute("playlist", playlist);
        getSession().setAttribute("playlistContent", tracks);
    }
}