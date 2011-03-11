package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.UserAgent;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;

import java.util.HashMap;
import java.util.Map;

public class TrackRenderer implements Renderer<Map<String, Object>, Track> {
    public Map<String, Object> render(Track track) {
        Map result = new HashMap();
        result.put("album", track.getAlbum());
        result.put("artist", track.getArtist());
        result.put("comment", track.getComment());
        result.put("contentLength", track.getContentLength());
        result.put("contentType", track.getContentType());
        result.put("genre", track.getGenre());
        result.put("id", track.getId());
        result.put("imageHash", track.getImageHash());
        result.put("lastImageUpdate", track.getLastImageUpdate());
        result.put("mp4Codec", track.getMp4Codec());
        result.put("mediaType", track.getMediaType().name());
        result.put("videoType", track.getVideoType().name());
        result.put("series", track.getSeries());
        result.put("season", track.getSeason());
        result.put("episode", track.getEpisode());
        result.put("photoalbum", track.getPhotoAlbum());
        result.put("protected", track.isProtected());
        result.put("name", track.getName());
        result.put("playCount", track.getPlayCount());
        result.put("posNumber", track.getPosNumber());
        result.put("posSize", track.getPosSize());
        result.put("time", track.getTime());
        result.put("trackNumber", track.getTrackNumber());
        result.put("tsPlayed", track.getTsPlayed());
        result.put("tsUpdated", track.getTsUpdated());
        if (MyTunesRssWebUtils.isHttpLiveStreaming(MyTunesRssRemoteEnv.getRequest(), track, true)) {
            result.put("playbackUrl", MyTunesFunctions.httpLiveStreamUrl(MyTunesRssRemoteEnv.getRequest(), track, null));
        } else {
            result.put("playbackUrl", MyTunesFunctions.playbackUrl(MyTunesRssRemoteEnv.getRequest(), track, null));
        }
        result.put("downloadUrl", MyTunesFunctions.downloadUrl(MyTunesRssRemoteEnv.getRequest(), track, null));
        result.put("imageUrl", track.getImageHash() != null ? MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.ShowImage,
                                                                                             "hash=" + track.getImageHash()) : null);
        return result;
    }
}