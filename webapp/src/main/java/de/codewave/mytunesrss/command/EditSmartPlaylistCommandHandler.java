package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        List<SmartInfo> sortedInfos = new ArrayList<SmartInfo>(smartPlaylist.getSmartInfos());
        Collections.sort(sortedInfos, new Comparator<SmartInfo>() {
            public int compare(SmartInfo o1, SmartInfo o2) {
                return getSmartTypeSortValue(o1.getFieldType(), o1.isInvert()) - getSmartTypeSortValue(o2.getFieldType(), o2.isInvert());
            }
        });
        smartPlaylist.setSmartInfos(sortedInfos);
        getRequest().setAttribute("smartPlaylist", smartPlaylist);
        forward(MyTunesRssResource.EditSmartPlaylist);
    }

    static int getSmartTypeSortValue(SmartFieldType fieldType, boolean inverted) {
        int i = fieldType.ordinal() * 2;
        return inverted ? i + 1 : i;
    }

}
