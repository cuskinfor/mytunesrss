/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class LoadAndContinuePlaylistCommandHandler extends LoadPlaylistCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getStates().put("addToPlaylistMode", Boolean.TRUE);
        loadPlaylist();
        forward(MyTunesRssCommand.BrowseArtist);
    }
}