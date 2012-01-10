/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.EditPlaylistCommandHandler
 */
public class ShowEditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            forward(MyTunesRssResource.EditPlaylist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}