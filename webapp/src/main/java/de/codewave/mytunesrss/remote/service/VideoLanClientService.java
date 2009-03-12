package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.service.VideoLanClientService
 */
public class VideoLanClientService {
    private static VideoLanClientProcess myVlcProcess = new VideoLanClientProcess();

    public void start() throws IOException, InterruptedException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            myVlcProcess.start();
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public void stop() throws InterruptedException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            myVlcProcess.stop();
        } else {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    public String sendCommand(String command) throws IOException, IllegalAccessException, InterruptedException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return myVlcProcess.sendCommand(command);
        }
        throw new IllegalAccessException("Unauthorized");
    }
}