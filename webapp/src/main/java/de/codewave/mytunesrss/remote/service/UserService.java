package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.service.UserService
 */
public class UserService {

    public void saveSettings(Map settings) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            // todo remote-api
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Map loadSettings() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            // todo remote-api
        }
        throw new IllegalAccessException("Unauthorized");
    }
}