package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.User;

/**
 * de.codewave.mytunesrss.remote.Session
 */
public class Session {
    private String myId;
    private User myUser;
    private int myTimeout;
    private long myExpiration;

    public Session(String id, User user, int timeout) {
        myId = id;
        myUser = user;
        myTimeout = timeout;
    }

    public void touch() {
        if (!isExpired()) {
            myExpiration = System.currentTimeMillis() + (long)myTimeout;
        }
    }

    public void invalidate() {
        myExpiration = 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= myExpiration;
    }

    public String getId() {
        return myId;
    }

    public User getUser() {
        return myUser;
    }
}