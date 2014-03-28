/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.mediarenderercontrol.MediaRendererController;

public class ClearRemotePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        MediaRendererController.getInstance().clearPlaylist();
        forward(MyTunesRssCommand.ShowPortal);
    }
}
