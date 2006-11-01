package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.*;
import de.codewave.utils.swing.*;
import org.apache.commons.logging.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Log LOG = LogFactory.getLog(MyTunesRssUncaughtHandler.class);

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
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                new SupportContact().display(myParent, MyTunesRss.BUNDLE.getString("dialog.bugReport"), MyTunesRss.BUNDLE.getString(
                        "settings.supportBugInfo"));
            }
        });
        if (myTerminate) {
            System.exit(0);
        }
    }
}