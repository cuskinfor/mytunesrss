package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModalInfoDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModalInfoDialog.class);
    private volatile JDialog myInfo;
    private volatile Thread myThread;
    private AtomicBoolean myCancelled = new AtomicBoolean();

    public ModalInfoDialog(String message) {
        if (!MyTunesRssUtils.isHeadless()) {
            myInfo = new JDialog((Frame) null, true);
            myInfo.setUndecorated(true);
            myInfo.getContentPane().setLayout(new GridBagLayout());
            myInfo.getContentPane().add(new JLabel(message), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 40, 10, 40), 0, 0));
            myInfo.pack();
        }
    }

    public void show(final long delayMillis) {
        if (!MyTunesRssUtils.isHeadless()) {
            myThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(delayMillis);
                        if (!myCancelled.get() && !Thread.interrupted()) {
                            myInfo.setVisible(true);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.info("Interrupted while delaying.");
                    }
                }
            }, "MyTunesRSSModalInfoDialog");
            myThread.setDaemon(true); // make sure this does not prevent the JVM from exiting
            myThread.start();
            myInfo.setLocationRelativeTo(null);
        }
    }

    public void destroy() {
        if (!MyTunesRssUtils.isHeadless()) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    myCancelled.set(true);
                    myThread.interrupt();
                    try {
                        while (myThread.isAlive()) {
                            myInfo.dispose();
                            myThread.join(100);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.debug("Interrupted while waiting for thread to die.");
                    }
                }
            }, "MyTunesRSSModalInfoDialogKiller");
            thread.setDaemon(true);
            thread.start();
        }
    }
}
