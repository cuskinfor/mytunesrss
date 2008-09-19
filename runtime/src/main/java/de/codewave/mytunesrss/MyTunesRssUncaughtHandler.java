package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.SupportContact;
import de.codewave.utils.swing.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssUncaughtHandler.class);

    private boolean myTerminate;
    private JFrame myParent;

    public MyTunesRssUncaughtHandler(JFrame parent, boolean terminate) {
        myParent = parent;
        myTerminate = terminate;
    }

    public void uncaughtException(Thread t, final Throwable e) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Handling uncaught exception.", e);
        }
        MyTunesRss.ADMIN_NOTIFY.notifyInternalError(e);
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                new SupportContact().display(myParent, MyTunesRssUtils.getBundleString("dialog.bugReport"), MyTunesRssUtils.getBundleString(
                        "settings.supportBugInfo"));
            }
        });
        if (myTerminate) {
            MyTunesRssUtils.shutdown();
        }
    }
}