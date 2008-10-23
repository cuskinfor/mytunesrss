package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.TrackUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.EnhancedTracksRenderer
 */
public class EnhancedTracksRenderer implements Renderer<Map<String, Object>, TrackUtils.EnhancedTracks> {
    public Map<String, Object> render(TrackUtils.EnhancedTracks o) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("simple", o.isSimpleResult());
        map.put("tracks", RenderMachine.getInstance().render(o.getTracks()));
        return map;
    }
}