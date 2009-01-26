package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.EditSmartPlaylistCommandHandler
 */
public class EditSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        // todo: implement
        forward(MyTunesRssResource.EditSmartPlaylist);
    }
}