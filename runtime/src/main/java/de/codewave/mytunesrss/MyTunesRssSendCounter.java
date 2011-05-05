package de.codewave.mytunesrss;

import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;

/**
 * de.codewave.mytunesrss.MyTunesRssSendCounter
 */
public class MyTunesRssSendCounter implements StreamSender.ByteSentCounter {
    private SessionManager.SessionInfo mySessionInfo;
    private User myUser;
    private long myCount;

    public MyTunesRssSendCounter(User user, SessionManager.SessionInfo sessionInfo) {
        myUser = user;
        mySessionInfo = sessionInfo;
    }

    public void add(int count) {
        myCount += count;
        ((StreamSender.ByteSentCounter) mySessionInfo).add(count);
    }

    public void notifyBegin() {
    }

    public void notifyEnd() {
        StatisticsEventManager.getInstance().fireEvent(new DownloadEvent(myUser, myCount));
    }
}
