package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.AlbumRenderer
 */
public class AlbumRenderer implements Renderer<Map, Album> {
    public Map render(Album album) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", StringUtils.trimToEmpty(album.getName()));
        result.put("trackCount", album.getTrackCount());
        result.put("artist", StringUtils.trimToEmpty(album.getArtist()));
        result.put("artistCount", album.getArtistCount());
        result.put("imageUrl", album.isImage() ? MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.ShowAlbumImage, "album=" + album.getName()) : null);
        return result;
    }
}