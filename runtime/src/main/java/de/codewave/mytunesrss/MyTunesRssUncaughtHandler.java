package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssUncaughtHandler.class);

    private boolean myTerminate;

    public MyTunesRssUncaughtHandler(boolean terminate) {
        myTerminate = terminate;
    }

    public void uncaughtException(Thread t, final Throwable e) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Handling uncaught exception.", e);
        }
        MyTunesRss.ADMIN_NOTIFY.notifyInternalError(e);
        // TODO put into error queue?
        if (myTerminate) {
            MyTunesRssUtils.shutdownGracefully();
        }
    }
}