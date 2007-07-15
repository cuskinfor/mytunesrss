package de.codewave.mytunesrss;

import de.codewave.utils.swing.pleasewait.*;

/**
 * de.codewave.mytunesrss.MyTunesRssTask
 */
public abstract class MyTunesRssTask extends PleaseWaitTask {
    @Override
    public void handleException(Exception e) {
        new MyTunesRssUncaughtHandler(MyTunesRss.ROOT_FRAME, false).uncaughtException(Thread.currentThread(), e);
    }
}