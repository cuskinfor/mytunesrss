package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.MyTunesRss;

/**
 * de.codewave.mytunesrss.config.UserProxy
 */
public class UserProxy extends User {
    public UserProxy(String name) {
        super(name);
    }

    public User resolveUser() {
        return MyTunesRss.CONFIG.getUser(getName());
    }
}