package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.SaveSmartPlaylistCommandHandler
 */
public class SaveSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (StringUtils.isBlank(getRequestParameter("smartPlaylist.playlist.name", null))) {
            addError(new BundleError("error.needPlaylistNameForSave"));
        }
        Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
        if (isError()) {
            handleError();
            return;
        }
        Enumeration<String> namesEnum = getRequest().getParameterNames();
        while (namesEnum.hasMoreElements()) {
            String name = namesEnum.nextElement();
            if (name.startsWith("type_")) {
                String suffix = name.substring(5);
                SmartFieldType type = SmartFieldType.valueOf(getRequestParameter("type_" + suffix, null));
                String pattern = getRequestParameter("pattern_" + suffix, null);
                boolean invert = getBooleanRequestParameter("invert_" + suffix, false);
                if (StringUtils.isNotBlank(pattern)) {
                    if (type == SmartFieldType.sizeLimit) {
                        try {
                            Integer.parseInt(pattern);
                        } catch (NumberFormatException e) {
                            addError(new BundleError("error.illegalSmartPlaylistSizeLimit"));
                        }
                    }
                    smartInfos.add(new SmartInfo(type, pattern, invert));
                }
            }
        }
        if (isError()) {
            handleError();
            return;
        }
        SaveMyTunesSmartPlaylistStatement statement = new SaveMyTunesSmartPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter("smartPlaylist.playlist.userPrivate", false), smartInfos);
        statement.setId(getRequestParameter("smartPlaylist.playlist.id", null));
        statement.setName(getRequestParameter("smartPlaylist.playlist.name", null));
        statement.setTrackIds(Collections.<String>emptyList());
        getTransaction().executeStatement(statement);
        getTransaction().executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, statement.getPlaylistIdAfterExecute()));
        forward(MyTunesRssCommand.ShowPlaylistManager);
    }
    
    private void handleError() throws IOException, ServletException {
        getRequest().setAttribute("smartPlaylist", createRedisplayModel(null, null));
        forward(MyTunesRssResource.EditSmartPlaylist);
    }

    protected Map<String, Object> createRedisplayModel(String remove, Map<String, String> add) throws IOException, ServletException {
        Map<String, Object> playlistModel = new HashMap<String, Object>();
        playlistModel.put("name", getRequestParameter("smartPlaylist.playlist.name", null));
        playlistModel.put("id", getRequestParameter("smartPlaylist.playlist.id", null));
        playlistModel.put("userPrivate", getRequestParameter("smartPlaylist.playlist.userPrivate", null));
        List<Map<String, String>> smartInfos = new ArrayList<Map<String, String>>();
        Enumeration<String> namesEnum = getRequest().getParameterNames();
        while (namesEnum.hasMoreElements()) {
            String name = namesEnum.nextElement();
            if (name.startsWith("type_")) {
                String suffix = name.substring(5);
                if (!suffix.equals(remove)) {
                    Map<String, String> smartInfo = new HashMap<String, String>();
                    smartInfo.put("fieldType", getRequestParameter("type_" + suffix, ""));
                    smartInfo.put("pattern", getRequestParameter("pattern_" + suffix, ""));
                    smartInfo.put("invert", getRequestParameter("invert_" + suffix, ""));
                    smartInfos.add(smartInfo);
                }
            }
        }
        if (add != null) { 
            smartInfos.add(add);
        }
        Map<String, Object> smartPlaylistModel = new HashMap<String, Object>();
        smartPlaylistModel.put("smartInfos", smartInfos);
        smartPlaylistModel.put("playlist", playlistModel);
        sortModelSmartInfos(smartPlaylistModel);
        return smartPlaylistModel;
    }

    private void sortModelSmartInfos(Map<String, Object> model) {
        List<Map<String, String>> smartInfos = (List<Map<String, String>>) model.get("smartInfos"); 
        Collections.sort(smartInfos, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                try {
                    int v1 = EditSmartPlaylistCommandHandler.getSmartTypeSortValue(SmartFieldType.valueOf(o1.get("fieldType")), Boolean.valueOf(o1.get("invert")));
                    int v2 = EditSmartPlaylistCommandHandler.getSmartTypeSortValue(SmartFieldType.valueOf(o2.get("fieldType")), Boolean.valueOf(o2.get("invert")));
                    return v1 - v2;
                } catch (IllegalArgumentException e) {
                    return 0;
                }
            }
        });
    }
}
