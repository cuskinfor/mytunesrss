package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
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

    private VideoLanClient connect() throws IOException, InterruptedException, IllegalAccessException {
        VideoLanClient videoLanClient = new VideoLanClient();
        videoLanClient.connect(MyTunesRss.CONFIG.getVideoLanClientHost(), MyTunesRss.CONFIG.getVideoLanClientPort());
        return videoLanClient;
    }

    private void disconnect(VideoLanClient videoLanClient) throws InterruptedException, IllegalAccessException, IOException {
        videoLanClient.disconnect();
    }

    public String loadPlaylist(String item, boolean start) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("clear", "add " + item, start ? "start" : "stop");
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String clearPlaylist() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("clear");
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String play(int index) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("goto " + index);
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String pause() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("pause");
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String stop() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("stop");
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String next() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("next");
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String prev() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands("prev");
        } finally {
            disconnect(videoLanClient);
        }
    }

    public String sendCommand(String command) throws IOException, IllegalAccessException, InterruptedException {
        assertAuthenticated();
        VideoLanClient videoLanClient = connect();
        try {
            return videoLanClient.sendCommands(command);
        } finally {
            disconnect(videoLanClient);
        }
    }
}