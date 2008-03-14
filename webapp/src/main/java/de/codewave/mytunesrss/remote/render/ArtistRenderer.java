package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.datastore.statement.Artist;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.AlbumRenderer
 */
public class ArtistRenderer implements Renderer<Map, Artist> {
    public Map render(Artist artist) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", StringUtils.trimToEmpty(artist.getName()));
        result.put("albumCount", artist.getAlbumCount());
        result.put("trackCount", artist.getTrackCount());
        return result;
    }
}