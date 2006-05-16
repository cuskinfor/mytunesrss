/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class StartNewPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        List<Track> playlist = new ArrayList<Track>();
        getSession().setAttribute("playlist", new Playlist());
        getSession().setAttribute("playlistContent", playlist);
        forward(MyTunesRssCommand.BrowseArtist);
    }
}