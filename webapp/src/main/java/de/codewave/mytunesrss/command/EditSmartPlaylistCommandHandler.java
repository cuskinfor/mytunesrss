package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindSmartPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.SmartInfo;
import de.codewave.mytunesrss.datastore.statement.SmartPlaylist;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.EditSmartPlaylistCommandHandler
 */
public class EditSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        String playlistId = getRequestParameter("playlistId", null);
        SmartPlaylist smartPlaylist = new SmartPlaylist();
        smartPlaylist.setPlaylist(new Playlist());
        smartPlaylist.setSmartInfo(new SmartInfo());
        if (StringUtils.isNotBlank(playlistId)) {
            smartPlaylist = getTransaction().executeQuery(new FindSmartPlaylistQuery(playlistId)).nextResult();
        }
        getRequest().setAttribute("smartPlaylist", smartPlaylist);
        getRequest().setAttribute("fields", getFields());
        forward(MyTunesRssResource.EditSmartPlaylist);
    }

    static String[] getFields() {
        return new String[]{"smartPlaylist.smartInfo.albumPattern", "smartPlaylist.smartInfo.artistPattern", "smartPlaylist.smartInfo.composerPattern",
                "smartPlaylist.smartInfo.genrePattern",
                "smartPlaylist.smartInfo.seriesPattern", "smartPlaylist.smartInfo.titlePattern", "smartPlaylist.smartInfo.filePattern", "smartPlaylist.smartInfo.tagPattern", "smartPlaylist.smartInfo.commentPattern",
                "smartPlaylist.smartInfo.timeMin", "smartPlaylist.smartInfo.timeMax"};
    }
}