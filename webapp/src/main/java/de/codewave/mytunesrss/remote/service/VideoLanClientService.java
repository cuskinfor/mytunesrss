package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class VideoLanClientService {
    private static VideoLanClient myVideoLanClient = new VideoLanClient();

    private void assertAuthenticated() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user == null) {
            throw new IllegalAccessException("Unauthorized");
        }
    }


    public void connect(String host, int port) throws IOException, InterruptedException, IllegalAccessException {
        assertAuthenticated();
        myVideoLanClient.connect(host, port);
    }

    public void disconnect() throws InterruptedException, IllegalAccessException, IOException {
        assertAuthenticated();
        myVideoLanClient.disconnect();
    }

    public String add(String item, boolean stop) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        if (stop) {
            return myVideoLanClient.sendCommands("add " + item, "stop");
        } else {
            return myVideoLanClient.sendCommands("add " + item);
        }
    }

    public String enqueue(String item, boolean stop) throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        if (stop) {
            return myVideoLanClient.sendCommands("enqueue " + item, "stop");
        } else {
            return myVideoLanClient.sendCommands("enqueue " + item);
        }
    }

    public String clear() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands("clear");
    }

    public String play() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands("play");
    }

    public String pause() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands("pause");
    }

    public String stop() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands("stop");
    }

    public String next() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands("next");
    }

    public String prev() throws IllegalAccessException, IOException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands("prev");
    }

    public String sendCommand(String command) throws IOException, IllegalAccessException, InterruptedException {
        assertAuthenticated();
        return myVideoLanClient.sendCommands(command);
    }
}