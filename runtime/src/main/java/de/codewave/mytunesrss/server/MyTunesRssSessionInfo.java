package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;

import javax.servlet.http.HttpServletRequest;

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
        getUser().setDownBytes(getUser().getDownBytes() + bytes);
        getUser().setQuotaDownBytes(getUser().getQuotaDownBytes() + bytes);
    }

    public void notifyBegin() {
        // intentionally left blank
    }

    public void notifyEnd() {
        // intentionally left blank
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