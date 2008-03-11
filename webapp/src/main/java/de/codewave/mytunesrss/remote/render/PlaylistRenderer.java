package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.datastore.statement.Playlist;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.PlaylistRenderer
 */
public class PlaylistRenderer implements Renderer<Map<String, Object>, Playlist> {
    public Map<String, Object> render(Playlist playlist) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", StringUtils.trimToEmpty(playlist.getId()));
        result.put("name", StringUtils.trimToEmpty(playlist.getName()));
        result.put("count", playlist.getTrackCount());
        return result;
    }
}