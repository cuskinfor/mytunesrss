package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUncaughtHandler.class);

    public void uncaughtException(Thread t, final Throwable e) {
        try {
            LOGGER.error("Uncaught exception in thread \"" + t.getName() + "\".", e);
            if (!MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_HEADLESS) && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "uncaughtError.message", getLogFilePath()), MyTunesRssUtils.getBundleString(Locale.getDefault(), "uncaughtError.title"), JOptionPane.ERROR_MESSAGE);
            }
            MyTunesRss.ADMIN_NOTIFY.notifyInternalError(e);
        } finally {
            MyTunesRssUtils.shutdownGracefully();
        }
    }

    private String getLogFilePath() {
        return MyTunesRss.CACHE_DATA_PATH + "/MyTunesRSS.log";
    }
}