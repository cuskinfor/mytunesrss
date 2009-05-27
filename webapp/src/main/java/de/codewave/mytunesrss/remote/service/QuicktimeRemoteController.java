package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.mytunesrss.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class QuicktimeRemoteController implements RemoteController {

    public void loadPlaylist(String playlistId) throws IOException {
        loadItem("playlist=" + playlistId);
    }

    private void loadItem(String pathInfo) throws IOException {
        String url = MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, pathInfo + "/type=" + WebConfig.PlaylistType.M3u) + "/mytunesrss.m3u";
        loadUrl(url);
    }

    private void loadUrl(String url) throws IOException {
        AppleScriptClient client = new AppleScriptClient("QuickTime Player");
        client.executeAppleScript(
                "stop every document",
                "close every document",
                "open location \"" + url + "\"",
                "get streaming status code of document 1"
        );
    }

    public void loadAlbum(String albumName) throws IOException {
        loadItem("album=" + MyTunesRssBase64Utils.encode(albumName));
    }

    public void loadArtist(String artistName, boolean fullAlbums) throws IOException {
        if (fullAlbums) {
            loadItem("fullAlbums=true/artist=" + MyTunesRssBase64Utils.encode(artistName));
        } else {
            loadItem("artist=" + MyTunesRssBase64Utils.encode(artistName));
        }
    }

    public void loadGenre(String genreName) throws IOException {
        loadItem("genre=" + MyTunesRssBase64Utils.encode(genreName));
    }

    public void loadTrack(String trackId) throws IOException, SQLException {
        Collection<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForId(new String[]{trackId})).getResults();
        loadUrl(MyTunesFunctions.playbackUrl(MyTunesRssRemoteEnv.getRequest(), tracks.iterator().next(), null));
    }

    public void loadTracks(String[] trackIds) throws IOException {
        loadItem("tracklist=" + StringUtils.join(trackIds, ","));
    }

    public void clearPlaylist() throws IOException {
        new AppleScriptClient("QuickTime Player").executeAppleScript("close every document");
    }

    public void play(int index) throws IOException {
        if (index > 0) {
            new AppleScriptClient("QuickTime Player").executeAppleScript(
                    "activate",
                    "set current time of document 1 to start time of track " + index + " of document 1",
                    "play document 1"
            );
        } else {
            new AppleScriptClient("QuickTime Player").executeAppleScript(
                    "activate",
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
        String appleScriptResponse = new AppleScriptClient("QuickTime Player").executeAppleScript("get documents");
        if ("{}".equals(appleScriptResponse)) {
            return null; // nothing playing
        }
        appleScriptResponse = new AppleScriptClient("QuickTime Player").executeAppleScript(
                "set currentpos to current time of document 1",
                "set tracknumber to 0",
                "set tracklist to get start time of every track of document 1 & 0",
                "repeat with i from 1 to count of my tracklist",
                "  if (currentpos < item i of my tracklist) then",
                "    set tracknumber to i",
                "    exit repeat",
                "  end if",
                "end repeat",
                "get (i - 1) & currentpos - (item (i - 1) of my tracklist) & duration of track (i - 1) of document 1 & playing of document 1 & sound volume of document 1"
        );
        RemoteTrackInfo trackInfo = new RemoteTrackInfo();
        appleScriptResponse = StringUtils.removeEnd(StringUtils.removeStart(StringUtils.trimToEmpty(appleScriptResponse), "{"), "}");
        String[] splitted = StringUtils.split(appleScriptResponse, ",");
        if (splitted.length != 5) {
            return null;
        }
        trackInfo.setCurrentTrack(Integer.parseInt(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[0]), "-1")));
        trackInfo.setCurrentTime(Integer.parseInt(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[1]), "-1")));
        trackInfo.setLength(Integer.parseInt(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[2]), "-1")));
        trackInfo.setPlaying(Boolean.parseBoolean(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[3]), "false")));
        trackInfo.setVolume((int) ((Float.parseFloat(StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(splitted[4]), "-1.0")) * 100.0) / 256.0));
        return trackInfo;
    }

    public void setVolume(int percentage) throws Exception {
        int normalizedPercentage = Math.min(Math.max(0, percentage), 100);
        new AppleScriptClient("QuickTime Player").executeAppleScript("set sound volume of document 1 to " + (int) (((256.0 * (float) normalizedPercentage) / 100.0)));
    }

    public void setFullscreen(boolean fullscreen) throws Exception {
        if (fullscreen) {
            new AppleScriptClient("QuickTime Player").executeAppleScript("stop document 1", "present document 1 scale screen", "play document 1");
        } else {
            new AppleScriptClient("QuickTime Player").executeAppleScript("stop document 1", "play document 1");
        }
    }

    public void shuffle() throws Exception {
        // todo: implement method
        throw new UnsupportedOperationException("method shuffle of class QuicktimeRemoteController is not yet implemented!");
    }
}