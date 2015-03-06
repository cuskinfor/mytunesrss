package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.User;
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

    @Override
    public void add(int count) {
        myCount += count;
        ((StreamSender.ByteSentCounter) mySessionInfo).add(count);
    }

    @Override
    public void notifyBegin() {
    }

    @Override
    public void notifyEnd() {
        StatisticsEventManager.getInstance().fireEvent(new DownloadEvent(myUser.getName(), null,  myCount));
    }
}
