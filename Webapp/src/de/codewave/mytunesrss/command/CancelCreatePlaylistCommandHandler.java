/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

/**
 * de.codewave.mytunesrss.command.CancelCreatePlaylistCommandHandler
 */
public class CancelCreatePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getSession().removeAttribute("playlist");
        getSession().removeAttribute("playlistContent");
        forward(MyTunesRssCommand.ShowPortal);
    }
}