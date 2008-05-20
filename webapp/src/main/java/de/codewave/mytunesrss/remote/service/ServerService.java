package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

import java.util.List;

/**
 * de.codewave.mytunesrss.remote.service.ServerService
 */
public class ServerService {

    public List<String> discoverOtherServers() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            // todo remote-api
        }
        throw new IllegalAccessException("Unauthorized");
    }
}