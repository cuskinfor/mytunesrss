package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * de.codewave.mytunesrss.remote.Session
 */
public class Session {
    private String myId;
    private User myUser;
    private int myTimeout;
    private long myExpiration;
    private Map<String, Object> myAttributes = new HashMap<String, Object>();

    public Session(String id, User user, int timeout) {
        myId = id;
        myUser = user;
        myTimeout = timeout;
        myExpiration = System.currentTimeMillis() + (long)timeout;
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

    public Object setAttribute(String key, Object value) {
        return myAttributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return myAttributes.get(key);
    }

    public Object removeAttribute(String key) {
        return myAttributes.remove(key);
    }
}