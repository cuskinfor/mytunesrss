/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class LoadAndEditPlaylistCommandHandler extends LoadPlaylistCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            loadPlaylist();
            getStates().put("addToPlaylistMode", Boolean.TRUE);
            forward(MyTunesRssCommand.EditPlaylist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}