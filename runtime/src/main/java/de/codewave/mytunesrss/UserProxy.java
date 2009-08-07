package de.codewave.mytunesrss;

/**
 * de.codewave.mytunesrss.UserProxy
 */
public class UserProxy extends User {
    public UserProxy(String name) {
        super(name);
    }

    public User resolveUser() {
        return MyTunesRss.CONFIG.getUser(getName());
    }
}