/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;

public class ClearRemotePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        MyTunesRss.VLC_PLAYER.clearPlaylist();
        forward(MyTunesRssCommand.ShowPortal);
    }
}
