package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.datastore.statement.Genre;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.GenreRenderer
 */
public class GenreRenderer implements Renderer<Map, Genre> {
    public Map render(Genre genre) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", StringUtils.trimToEmpty(genre.getName()));
        result.put("trackCount", genre.getTrackCount());
        result.put("albumCount", genre.getAlbumCount());
        result.put("artistCount", genre.getArtistCount());
        return result;
    }
}