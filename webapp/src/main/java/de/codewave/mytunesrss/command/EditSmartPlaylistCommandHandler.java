package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * de.codewave.mytunesrss.command.EditSmartPlaylistCommandHandler
 */
public class EditSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        String playlistId = getRequestParameter("playlistId", null);
        SmartPlaylist smartPlaylist = new SmartPlaylist();
        smartPlaylist.setPlaylist(new Playlist());
        smartPlaylist.setSmartInfos(new ArrayList<SmartInfo>());
        if (StringUtils.isNotBlank(playlistId)) {
            smartPlaylist = getTransaction().executeQuery(new FindSmartPlaylistQuery(playlistId));
        }
        getRequest().setAttribute("smartPlaylist", smartPlaylist);
        forward(MyTunesRssResource.EditSmartPlaylist);
    }

}
