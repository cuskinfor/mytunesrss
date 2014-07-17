package de.codewave.mytunesrss;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUncaughtHandler.class);

    private AtomicBoolean executed = new AtomicBoolean(false);
    
    public void uncaughtException(Thread t, final Throwable e) {
        if (!executed.getAndSet(true)) {
            String message = "Uncaught exception in thread \"" + t.getName() + "\": \"" + e.getMessage() + "\".";
            try {
                LOGGER.error("Uncaught exception in thread \"" + t.getName() + "\".", e);
                message = MyTunesRssUtils.getBundleString(Locale.getDefault(), "uncaughtError.message", getLogFilePath());
                if (e instanceof OutOfMemoryError && StringUtils.isNotBlank(MyTunesRss.HEAPDUMP_FILENAME) && new File(MyTunesRss.HEAPDUMP_FILENAME).isFile()) {
                    message = MyTunesRssUtils.getBundleString(Locale.getDefault(), "uncaughtError.oom.message", getLogFilePath(), MyTunesRss.HEAPDUMP_FILENAME);
                }
                MyTunesRss.ADMIN_NOTIFY.notifyInternalError(e);
            } finally {
                MyTunesRssUtils.shutdownGracefully(message);
            }
        }
    }

    private String getLogFilePath() {
        return MyTunesRss.CACHE_DATA_PATH + "/MyTunesRSS.log";
    }
}
