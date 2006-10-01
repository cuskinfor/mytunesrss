package de.codewave.mytunesrss;

import de.codewave.utils.swing.*;
import org.apache.commons.logging.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.MyTunesRssUncaughtHandler
 */
public class MyTunesRssUncaughtHandler implements Thread.UncaughtExceptionHandler {
    private static final Log LOG = LogFactory.getLog(MyTunesRssUncaughtHandler.class);

    private JDialog myDialog;
    private JOptionPane myPane;
    private boolean myTerminate;

    public MyTunesRssUncaughtHandler(JFrame parent, boolean terminate) {
        myPane = SwingUtils.createMaxLengthOptionPane(MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
        myPane.setMessageType(JOptionPane.ERROR_MESSAGE);
        String okButton = "Ok";
        myPane.setInitialValue(okButton);
        myDialog = myPane.createDialog(parent, "Fatal error");
        myDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        myTerminate = terminate;
    }

    public void uncaughtException(Thread t, final Throwable e) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Handling uncaught exception.", e);
        }
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                myPane.setMessage(MyTunesRss.BUNDLE.getString("error.uncaughtException"));
                SwingUtils.packAndShowRelativeTo(myDialog, myDialog.getParent());
            }
        });
        if (myTerminate) {
            System.exit(0);
        }
    }
}