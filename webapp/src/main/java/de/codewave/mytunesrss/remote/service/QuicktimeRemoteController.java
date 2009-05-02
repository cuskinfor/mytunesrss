package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class QuicktimeRemoteController implements RemoteController {

    public void loadPlaylist(String playlistId, boolean start) throws IOException {
        loadItem("playlist=" + playlistId, start);
    }

    private void loadItem(String pathInfo, boolean start) throws IOException {
        String url = MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, pathInfo + "/type=" + WebConfig.PlaylistType.M3u) + "/mytunesrss.m3u";
        loadUrl(url, start);
    }

    private void loadUrl(String url, boolean start) throws IOException {
        AppleScriptClient client = new AppleScriptClient("QuickTime Player");
        if (start) {
            client.executeAppleScript(
                    "stop every document",
                    "close every document",
                    "open location \"" + url + "\"",
                    "activate",
                    "present document 1 scale screen mode normal",
                    "play document 1"
            );
        } else {
            client.executeAppleScript(
                    "stop every document",
                    "close every document",
                    "open location \"" + url + "\""
            );
        }
    }

    public void loadAlbum(String albumName, boolean start) throws IOException {
        loadItem("album=" + MyTunesRssBase64Utils.encode(albumName), start);
    }

    public void loadArtist(String artistName, boolean fullAlbums, boolean start) throws IOException {
        if (fullAlbums) {
            loadItem("fullAlbums=true/artist=" + MyTunesRssBase64Utils.encode(artistName), start);
        } else {
            loadItem("artist=" + MyTunesRssBase64Utils.encode(artistName), start);
        }
    }

    public void loadGenre(String genreName, boolean start) throws IOException {
        loadItem("genre=" + MyTunesRssBase64Utils.encode(genreName), start);
    }

    public void loadTrack(String trackId, boolean start) throws IOException {
        loadUrl(MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.PlayTrack, "/track=" + trackId), start);
    }

    public void loadTracks(String[] trackIds, boolean start) throws IOException {
        loadItem("tracklist=" + StringUtils.join(trackIds, ","), start);
    }

    public void clearPlaylist() throws IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript("close every document");
    }

    public void play(int index) throws IOException {
        if (index > 0) {
            new AppleScriptClient("QuickTime Player").executeAppleScript(
                    "activate",
                    "present document 1 scale screen mode normal",
                    "set current time of document 1 to start time of track " + index + " of document 1",
                    "play document 1"
            );
        } else {
            new AppleScriptClient("QuickTime Player").executeAppleScript(
                    "activate",
                    "present document 1 scale screen mode normal",
                    "play document 1"
            );
        }
    }

    public void pause() throws IllegalAccessException, IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript("pause document 1");
    }

    public void stop() throws IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript("stop document 1");
        jumpTo(0);
    }

    public void next() throws IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript(
                "set currentpos to current time of document 1",
                "set tracklist to get start time of every track of document 1",
                "repeat with i from 1 to count of my tracklist",
                "  if (currentpos < item i of my tracklist) then",
                "    set current time of document 1 to item i of my tracklist",
                "    exit repeat",
                "  end if",
                "end repeat",
                "play document 1"
        );
    }

    public void prev() throws IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript(
                "set currentpos to current time of document 1",
                "set tracklist to get start time of every track of document 1",
                "repeat with i from 1 to count of my tracklist",
                "  if (currentpos < item i of my tracklist) then",
                "    if (i > 2) then",
                "      set current time of document 1 to item (i - 2) of my tracklist",
                "    end if",
                "    exit repeat",
                "  end if",
                "end repeat",
                "play document 1"
        );
    }

    public void jumpTo(int percentage) throws IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript(
                "set currentpos to current time of document 1",
                "set tracknumber to 0",
                "set tracklist to get start time of every track of document 1",
                "repeat with i from 1 to count of my tracklist",
                "  if (currentpos < item i of my tracklist) then",
                "    set tracknumber to i",
                "    exit repeat",
                "  end if",
                "end repeat",
                "set current time of document 1 to item (i - 1) of my tracklist + (duration of track (i - 1) of document 1 * " + ((double) percentage / 100.0) + ")"
        );
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws IOException {
        String appleScriptResponse = new AppleScriptClient("QuickTime Player").executeAppleScript(
                "set currentpos to current time of document 1",
                "set tracknumber to 0",
                "set tracklist to get start time of every track of document 1",
                "repeat with i from 1 to count of my tracklist",
                "  if (currentpos < item i of my tracklist) then",
                "    set tracknumber to i",
                "    exit repeat",
                "  end if",
                "end repeat",
                "get (i - 1) & currentpos - (item (i - 1) of my tracklist) & duration of track (i - 1) of document 1 & playing of document 1"
        );
        RemoteTrackInfo trackInfo = new RemoteTrackInfo();
        appleScriptResponse = StringUtils.removeEnd(StringUtils.removeStart(StringUtils.trimToEmpty(appleScriptResponse), "{"), "}");
        String[] splitted = StringUtils.split(appleScriptResponse, ",");
        trackInfo.setCurrentTrack(Integer.parseInt(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[0]), "-1")));
        trackInfo.setCurrentTime(Integer.parseInt(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[1]), "-1")));
        trackInfo.setLength(Integer.parseInt(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[2]), "-1")));
        trackInfo.setPlaying(Boolean.parseBoolean(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[3]), "false")));
        return trackInfo;
    }

    /**
     * Get the feature set of the quicktime remote contol service.
     *
     * @return The feature set.
     */
    public RemoteControlFeatures getFeatures() {
        return new RemoteControlFeatures(true, true);
    }
}