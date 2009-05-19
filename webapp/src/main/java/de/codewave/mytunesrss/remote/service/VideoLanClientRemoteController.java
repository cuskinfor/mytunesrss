package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.WebConfig;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class VideoLanClientRemoteController implements RemoteController {
    private VideoLanClient getVideoLanClient() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = new VideoLanClient();
        videoLanClient.connect(MyTunesRss.CONFIG.getVideoLanClientHost(), MyTunesRss.CONFIG.getVideoLanClientPort());
        return videoLanClient;
    }

    public void loadPlaylist(String playlistId, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        loadItem("playlist=" + playlistId, start);
    }

    private void loadItem(String pathInfo, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        String url = MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, pathInfo + "/type=" + WebConfig.PlaylistType.M3u) + "/mytunesrss.m3u";
        loadUrl(url, start);
    }

    private void loadUrl(String url, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            if (start) {
                videoLanClient.sendCommands("clear", "add " + url);
                if (!StringUtils.contains(videoLanClient.sendCommands("status"), "play state")) {
                    videoLanClient.sendCommands("pause", "goto 0");
                }
                videoLanClient.sendCommands("f on");
            } else {
                videoLanClient.sendCommands("clear", "add " + url, "stop");
            }
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void loadAlbum(String albumName, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        loadItem("album=" + MyTunesRssBase64Utils.encode(albumName), start);
    }

    public void loadArtist(String artistName, boolean fullAlbums, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        if (fullAlbums) {
            loadItem("fullAlbums=true/artist=" + MyTunesRssBase64Utils.encode(artistName), start);
        } else {
            loadItem("artist=" + MyTunesRssBase64Utils.encode(artistName), start);
        }
    }

    public void loadGenre(String genreName, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        loadItem("genre=" + MyTunesRssBase64Utils.encode(genreName), start);
    }

    public void loadTrack(String trackId, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        loadUrl(MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.PlayTrack, "/track=" + trackId), start);
    }

    public void loadTracks(String[] trackIds, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        loadItem("tracklist=" + StringUtils.join(trackIds, ","), start);
    }

    public void clearPlaylist() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            videoLanClient.sendCommands("clear");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void play(int index) throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            if (index == 0) {
                if (!StringUtils.contains(videoLanClient.sendCommands("status"), "play state")) {
                    videoLanClient.sendCommands("pause");
                }
            } else {
                videoLanClient.sendCommands("goto " + (index - 1));
            }
            videoLanClient.sendCommands("f on");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void pause() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            if (StringUtils.contains(videoLanClient.sendCommands("status"), "play state")) {
                videoLanClient.sendCommands("pause");
            }
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void stop() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            if (StringUtils.contains(videoLanClient.sendCommands("status"), "play state")) {
                videoLanClient.sendCommands("seek 0", "pause");
            } else {
                videoLanClient.sendCommands("pause", "seek 0", "pause");
            }
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void next() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            videoLanClient.sendCommands("next", "f on");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void prev() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            videoLanClient.sendCommands("prev", "f on");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public void jumpTo(int percentage) throws Exception {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            int max = Integer.parseInt(videoLanClient.sendCommands("get_length"));
            videoLanClient.sendCommands("seek " + (max * percentage) / 100);
        } finally {
            videoLanClient.disconnect();
        }
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws Exception {
        VideoLanClient videoLanClient = getVideoLanClient();
        RemoteTrackInfo info = new RemoteTrackInfo();
        try {
            String currentTrack = StringUtils.trim(StringUtils.substringBefore(StringUtils.substringAfter(videoLanClient.sendCommands("status"), "new input:"), ")"));
            if (StringUtils.isNotBlank(currentTrack)) {
                info.setPlaying(true);
                int i = 0;
                String[] playlist = StringUtils.split(videoLanClient.sendCommands("playlist"), "|");
                for (String entry : playlist) {
                    if (StringUtils.isNotBlank(entry)) {
                        if (StringUtils.contains(entry, currentTrack)) {
                            info.setCurrentTrack((i / 2) + 1);
                            break;
                        }
                        i++;
                    }
                }
                info.setLength(Integer.parseInt(videoLanClient.sendCommands("get_length")));
                info.setCurrentTime(Integer.parseInt(videoLanClient.sendCommands("get_time")));
            }
        } finally {
            videoLanClient.disconnect();
        }
        return info;
    }

    public String sendCommand(String command) throws IOException, IllegalAccessException, InterruptedException {
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands(command);
        } finally {
            videoLanClient.disconnect();
        }
    }
}