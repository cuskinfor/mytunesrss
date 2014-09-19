/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.rest.resource.EditPlaylistResource;
import org.apache.commons.lang3.StringUtils;

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
            List<Track> tracks = new ArrayList<>(getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(),
                                                                                                                playlistId,
                                                                                                                null)).getResults());
            getRequest().getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST, playlist);
            getRequest().getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS, tracks);
        } else {
            throw new IllegalArgumentException("Missing parameter!");
        }
    }
}
