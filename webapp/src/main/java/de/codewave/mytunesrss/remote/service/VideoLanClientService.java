package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class VideoLanClientService {
    private void assertAuthenticated() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user == null) {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    private VideoLanClient getVideoLanClient() throws IllegalAccessException, IOException, InterruptedException {
        VideoLanClient videoLanClient = new VideoLanClient();
        videoLanClient.connect(MyTunesRss.CONFIG.getVideoLanClientHost(), MyTunesRss.CONFIG.getVideoLanClientPort());
        return videoLanClient;
    }

    public String loadPlaylist(String playlistId, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("playlist=" + playlistId, start);
    }

    private String loadItem(String pathInfo, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        String url = MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, pathInfo);
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            if (start) {
                return videoLanClient.sendCommands("clear", "add " + url);
            } else {
                return videoLanClient.sendCommands("clear", "add " + url, "stop");
            }
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String loadAlbum(String albumName, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("album=" + MyTunesRssBase64Utils.encode(albumName), start);
    }

    public String loadArtist(String artistName, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        return loadItem("artist=" + MyTunesRssBase64Utils.encode(artistName), start);
    }

    public String clearPlaylist() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands("clear");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String play(int index) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands("goto " + index);
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String pause() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands("pause");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String stop() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands("stop");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String next() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands("next");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String prev() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands("prev");
        } finally {
            videoLanClient.disconnect();
        }
    }

    public String sendCommand(String command) throws IOException, IllegalAccessException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = getVideoLanClient();
        try {
            return videoLanClient.sendCommands(command);
        } finally {
            videoLanClient.disconnect();
        }
    }
}