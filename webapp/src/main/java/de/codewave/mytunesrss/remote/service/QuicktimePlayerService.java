package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class QuicktimePlayerService {
    private void assertAuthenticated() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user == null) {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public String loadPlaylist(String playlistId, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("playlist=" + playlistId, start);
    }

    private String loadItem(String pathInfo, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        String url = MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, pathInfo + "/type=" + WebConfig.PlaylistType.M3u);
        AppleScriptClient client = new AppleScriptClient();
        if (start) {
            client.executeAppleScript("tell application \"QuickTime Player\"", "launch", "activate", "stop every document", "close every document", "open location \"" + url + "\"", "present document 1 scale screen mode normal", "play document 1", "end tell");
        } else {
            client.executeAppleScript("tell application \"QuickTime Player\"", "launch", "activate", "stop every document", "close every document", "open location \"" + url + "\"", "present document 1 scale screen mode normal", "end tell");
        }
        return null;
    }

    public String loadAlbum(String albumName, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("album=" + MyTunesRssBase64Utils.encode(albumName), start);
    }

    public String loadArtist(String artistName, boolean fullAlbums, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        if (fullAlbums) {
            return loadItem("fullAlbums=true/artist=" + MyTunesRssBase64Utils.encode(artistName), start);
        } else {
            return loadItem("artist=" + MyTunesRssBase64Utils.encode(artistName), start);
        }
    }

    public String loadGenre(String genreName, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("genre=" + MyTunesRssBase64Utils.encode(genreName), start);
    }

    public String loadTrack(String trackId, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("track=" + trackId, start);
    }

    public String loadTracks(String[] trackIds, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("tracklist=" + StringUtils.join(trackIds, ","), start);
    }

    public String clearPlaylist() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method clearPlaylist of class QuicktimePlayerService is not yet implemented!");
    }

    public String play(int index) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method play of class QuicktimePlayerService is not yet implemented!");
    }

    public String pause() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method pause of class QuicktimePlayerService is not yet implemented!");
    }

    public String stop() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method stop of class QuicktimePlayerService is not yet implemented!");
    }

    public String next() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method next of class QuicktimePlayerService is not yet implemented!");
    }

    public String prev() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method prev of class QuicktimePlayerService is not yet implemented!");
    }

    public String sendCommand(String command) throws IOException, IllegalAccessException, InterruptedException {
        assertAuthenticated();
        // todo: implement method
        throw new UnsupportedOperationException("method sendCommand of class QuicktimePlayerService is not yet implemented!");
    }
}