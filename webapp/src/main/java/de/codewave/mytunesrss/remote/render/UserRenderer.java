package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.TranscoderConfig;
import de.codewave.mytunesrss.config.User;

import java.util.*;

/**
 * de.codewave.mytunesrss.remote.render.UserRenderer
 */
public class UserRenderer implements Renderer<Map<String, Object>, User> {
    public Map<String, Object> render(User user) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("maxZipEntries", user.getMaximumZipEntries());
        result.put("name", user.getName());
        result.put("lastFmUser", user.getLastFmUsername());
        result.put("quota", user.isQuota());
        result.put("quotaExceeded", user.isQuotaExceeded());
        result.put("lastFmAccount", user.isLastFmAccount());
        Map<String, Boolean> permissions = new HashMap<String, Boolean>();
        result.put("permissions", permissions);
        permissions.put("changeEmail", user.isChangeEmail());
        permissions.put("changePassword", user.isChangePassword());
        permissions.put("editLastFmAccount", user.isEditLastFmAccount());
        permissions.put("createPlaylists", user.isCreatePlaylists());
        permissions.put("download", user.isDownload());
        permissions.put("player", user.isPlayer());
        permissions.put("playlist", user.isPlaylist());
        permissions.put("rss", user.isRss());
        permissions.put("transcoder", user.isTranscoder());
        permissions.put("upload", user.isUpload());
        if (user.isTranscoder() && !user.isForceTranscoders()) {
            List<String> transcoderNames = new ArrayList<String>();
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                transcoderNames.add(config.getName());
            }
            Collections.sort(transcoderNames);
            if (!transcoderNames.isEmpty()) {
                result.put("transcoderNames", transcoderNames);
            }
        }
        return result;
    }
}