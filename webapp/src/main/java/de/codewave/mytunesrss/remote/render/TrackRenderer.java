package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.command.MyTunesRssCommand;

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
        result.put("imageCount", track.getImageCount());
        result.put("lastImageUpdate", track.getLastImageUpdate());
        result.put("mp4Codec", track.getMp4Codec());
        result.put("name", track.getName());
        result.put("playCount", track.getPlayCount());
        result.put("posNumber", track.getPosNumber());
        result.put("posSize", track.getPosSize());
        result.put("time", track.getTime());
        result.put("trackNumber", track.getTrackNumber());
        result.put("tsPlayed", track.getTsPlayed());
        result.put("tsUpdated", track.getTsUpdated());
        result.put("playbackUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.PlayTrack, "track=" + track.getId()));
        result.put("downloadUrl", MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.DownloadTrack, "track=" + track.getId()));
        result.put("imageUrl", track.getImageCount() > 0 ? MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.ShowTrackImage, "track=" + track.getId()) : null);
        return result;
    }
}