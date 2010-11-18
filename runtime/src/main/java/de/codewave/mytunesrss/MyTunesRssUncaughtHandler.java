package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUncaughtHandler.class);

    private boolean myTerminate;

    public MyTunesRssUncaughtHandler(boolean terminate) {
        myTerminate = terminate;
    }

    public void uncaughtException(Thread t, final Throwable e) {
        LOGGER.error("Uncaught exception in thread \"" + t.getName() + "\".", e);
        MyTunesRss.ADMIN_NOTIFY.notifyInternalError(e);
        MyTunesRss.UNHANDLED_EXCEPTION.set(true);
        if (myTerminate) {
            MyTunesRssUtils.shutdownGracefully();
        }
    }
}