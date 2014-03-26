/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.remotecontrol.MediaRendererRemoteController;

public class ClearRemotePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        MediaRendererRemoteController.getInstance().clearPlaylist();
        forward(MyTunesRssCommand.ShowPortal);
    }
}
