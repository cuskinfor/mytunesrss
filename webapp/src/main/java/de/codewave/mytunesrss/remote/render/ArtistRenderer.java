package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.Artist;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
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
        result.put("downloadUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.GetZipArchive, "artist=" + artist.getName()));
        result.put("m3uUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, "artist=" + artist.getName() + "/type=M3u"));
        result.put("xspfUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, "artist=" + artist.getName() + "/type=Xspf"));
        result.put("rssUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreateRss, "artist=" + artist.getName()));
        return result;
    }
}