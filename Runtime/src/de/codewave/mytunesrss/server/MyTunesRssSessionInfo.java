package de.codewave.mytunesrss.server;

import de.codewave.utils.servlet.*;
import de.codewave.mytunesrss.*;

import javax.servlet.http.*;

/**
 * de.codewave.mytunesrss.common.MyTunesRssSessionInfo
 */
public class MyTunesRssSessionInfo extends SessionManager.SessionInfo implements FileSender.ByteSentCounter {
    private long myBytesStreamed;
    private User myUser;

    public MyTunesRssSessionInfo(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    public long getBytesStreamed() {
        return myBytesStreamed;
    }

    public void addBytesStreamed(long bytes) {
        myBytesStreamed += bytes;
    }

    public void add(int i) {
        addBytesStreamed(i);
    }

    public User getUser() {
        return myUser;
    }

    public void setUser(User user) {
        myUser = user;
    }
}