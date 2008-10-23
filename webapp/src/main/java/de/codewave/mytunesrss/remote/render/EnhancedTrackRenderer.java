package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.TrackUtils;

import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.EnhancedTrackRenderer
 */
public class EnhancedTrackRenderer implements Renderer<Map<String, Object>, TrackUtils.EnhancedTrack> {
    private TrackRenderer myTrackRenderer = new TrackRenderer();

    public Map<String, Object> render(TrackUtils.EnhancedTrack track) {
        Map<String, Object> map = myTrackRenderer.render(track);
        map.put("newSection", track.isNewSection());
        map.put("continuation", track.isContinuation());
        map.put("simple", track.isSimple());
        map.put("sectionIds", track.getSectionIds());
        map.put("sectionPlaylistId", track.getSectionPlaylistId());
        return map;
    }
}