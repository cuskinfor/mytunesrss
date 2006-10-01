/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class LoadAndEditPlaylistCommandHandler extends LoadPlaylistCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        loadPlaylist();
        forward(MyTunesRssCommand.EditPlaylist);
    }
}