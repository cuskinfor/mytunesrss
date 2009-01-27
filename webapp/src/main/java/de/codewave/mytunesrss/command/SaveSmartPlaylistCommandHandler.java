package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.SaveSmartPlaylistCommandHandler
 */
public class SaveSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        // todo: implement
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.SMART_INFO_CHANGED);
        forward(MyTunesRssResource.PlaylistManager);

        //in case of error: forward(MyTunesRssResource.EditSmartPlaylist);
    }
}