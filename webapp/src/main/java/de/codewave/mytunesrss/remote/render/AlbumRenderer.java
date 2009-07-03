package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.Album;
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
        result.put("imageUrl", album.getImageHash() != null ? MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.ShowImage, "hash=" + album.getImageHash()) :
                null);
        result.put("downloadUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.GetZipArchive, "album=" + album.getName()));
        result.put("m3uUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, "album=" + album.getName() + "/type=M3u"));
        result.put("xspfUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, "album=" + album.getName() + "/type=Xspf"));
        result.put("rssUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreateRss, "album=" + album.getName()));
        return result;
    }
}