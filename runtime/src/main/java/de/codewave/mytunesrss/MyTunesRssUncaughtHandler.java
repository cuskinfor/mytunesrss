package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUncaughtHandler.class);

    public static void addUncaughtExceptionNotification(Throwable e) {
        MyTunesRss.NOTIFICATION_QUEUE.add(new MyTunesRssNotification("Uncaught Exception", "An unexpected error occured.", e)); // TODO i18n
    }

    private boolean myTerminate;

    public MyTunesRssUncaughtHandler(boolean terminate) {
        myTerminate = terminate;
    }

    public void uncaughtException(Thread t, final Throwable e) {
        LOGGER.error("Uncaught exception in thread \"" + t.getName() + "\".", e);
        MyTunesRss.ADMIN_NOTIFY.notifyInternalError(e);
        addUncaughtExceptionNotification(e);
        if (myTerminate) {
            MyTunesRssUtils.shutdownGracefully();
        }
    }
}