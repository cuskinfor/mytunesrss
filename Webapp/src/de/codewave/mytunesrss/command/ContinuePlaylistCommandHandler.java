/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

/**
 * de.codewave.mytunesrss.command.ContinuePlaylistCommandHandler
 */
public class ContinuePlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        getStates().put("addToPlaylistMode", Boolean.TRUE);
        forward(MyTunesRssCommand.BrowseArtist);
    }
}