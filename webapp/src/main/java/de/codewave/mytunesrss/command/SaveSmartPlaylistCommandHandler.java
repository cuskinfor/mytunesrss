package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.SaveSmartPlaylistCommandHandler
 */
public class SaveSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    private static final int MAX_PATTERN_LENGTH = 255;

    @Override
    public void executeAuthorized() throws Exception {
        if (StringUtils.isBlank(getRequestParameter("smartPlaylist.playlist.name", null))) {
            addError(new BundleError("error.needPlaylistNameForSave"));
        }
        Collection<SmartInfo> smartInfos = new ArrayList<>();
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
                    if (type == SmartFieldType.sizeLimit || type == SmartFieldType.mintime || type == SmartFieldType.maxtime || type == SmartFieldType.recentlyPlayed || type == SmartFieldType.recentlyUpdated) {
                        try {
                            Integer.parseInt(pattern);
                        } catch (NumberFormatException ignored) {
                            switch (type) {
                                case sizeLimit:
                                    addError(new BundleError("error.illegalSmartPlaylistSizeLimit"));
                                    break;
                                case mintime:
                                    addError(new BundleError("error.illegalSmartPlaylistMintime"));
                                    break;
                                case maxtime:
                                    addError(new BundleError("error.illegalSmartPlaylistMaxtime"));
                                    break;
                                case recentlyPlayed:
                                    addError(new BundleError("error.illegalSmartPlaylistRecentlyPlayed"));
                                    break;
                                case recentlyUpdated:
                                    addError(new BundleError("error.illegalSmartPlaylistRecentlyUpdated"));
                                    break;
                                default:
                                    throw new IllegalStateException("Logic error validating values!");
                            }
                        }
                    }
                    if (pattern.length() > MAX_PATTERN_LENGTH) {
                        String typeBundleKey = "smartPlaylist.smartInfo." + type.name();
                        if (invert) {
                            typeBundleKey += ".not";
                        }
                        addError(new BundleError("error.smartPlaylistTextTooLong", getBundleString(typeBundleKey), MAX_PATTERN_LENGTH));
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
        if (!getAuthUser().isCreatePublicPlaylists()) {
            statement.setUserPrivate(true);
        }
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
        Map<String, Object> playlistModel = new HashMap<>();
        playlistModel.put("name", getRequestParameter("smartPlaylist.playlist.name", null));
        playlistModel.put("id", getRequestParameter("smartPlaylist.playlist.id", null));
        playlistModel.put("userPrivate", getRequestParameter("smartPlaylist.playlist.userPrivate", null));
        List<Map<String, String>> smartInfos = new ArrayList<>();
        Enumeration<String> namesEnum = getRequest().getParameterNames();
        while (namesEnum.hasMoreElements()) {
            String name = namesEnum.nextElement();
            if (name.startsWith("type_")) {
                String suffix = name.substring(5);
                if (!suffix.equals(remove)) {
                    Map<String, String> smartInfo = new HashMap<>();
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
        Map<String, Object> smartPlaylistModel = new HashMap<>();
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
                } catch (IllegalArgumentException ignored) {
                    return 0;
                }
            }
        });
    }
}
