package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.statistics.SessionStartEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
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
        if (myUser == null && user != null) {
            SessionStartEvent event = new SessionStartEvent(user.getName(), getSessionId());
            event.setEventTime(getConnectTime());
            StatisticsEventManager.getInstance().fireEvent(event);
        }
        myUser = user;
    }
}
