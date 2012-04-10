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
            smartPlaylist = getTransaction().executeQuery(new FindSmartPlaylistQuery(playlistId)).iterator().next();
        }
        getRequest().setAttribute("smartPlaylist", smartPlaylist);
        getRequest().setAttribute("fields", getFields());
        forward(MyTunesRssResource.EditSmartPlaylist);
    }

    static String[] getFields() {
        return new String[]{
                "smartPlaylist.smartFields." + SmartFieldType.album.name(),
                "smartPlaylist.smartFields." + SmartFieldType.artist.name(),
                "smartPlaylist.smartFields." + SmartFieldType.composer.name(),
                "smartPlaylist.smartFields." + SmartFieldType.genre.name(),
                "smartPlaylist.smartFields." + SmartFieldType.tvshow.name(),
                "smartPlaylist.smartFields." + SmartFieldType.title.name(),
                "smartPlaylist.smartFields." + SmartFieldType.file.name(),
                "smartPlaylist.smartFields." + SmartFieldType.tag.name(),
                "smartPlaylist.smartFields." + SmartFieldType.comment.name(),
                "smartPlaylist.smartFields." + SmartFieldType.mintime.name(),
                "smartPlaylist.smartFields." + SmartFieldType.maxtime.name()
        };
    }
}