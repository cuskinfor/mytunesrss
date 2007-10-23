/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.ContinuePlaylistCommandHandler
 */
public class ContinuePlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getStates().put("addToPlaylistMode", Boolean.TRUE);
            forward(MyTunesRssCommand.BrowseArtist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}