package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class DelayedModalInfo {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedModalInfo.class);
    
    private volatile boolean myDisposed;
    private volatile JDialog info;

    public DelayedModalInfo(String message) {
        if (!MyTunesRssUtils.isHeadless()) {
            info = new JDialog((Frame)null, true);
            info.setUndecorated(true);
            info.getContentPane().setLayout(new GridBagLayout());
            info.getContentPane().add(new JLabel(message), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 40, 10, 40), 0, 0));
            info.pack();
        }
    }
    
    public void show(final long delayMillis) {
        if (!MyTunesRssUtils.isHeadless()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        synchronized (info) {
                            info.wait(delayMillis);
                            if (!myDisposed) {
                                info.setVisible(true);
                                info.setLocationRelativeTo(null);
                            }
                        }
                    } catch (InterruptedException e) {
                        LOGGER.info("Interrupted while delaying display of modal info dialog.");
                    }
                }
            }, "MyTunesRSSModalInfoDialog").start();
        }
    }

    public void destroy() {
        if (!MyTunesRssUtils.isHeadless()) {
            synchronized (info) {
                info.dispose();
                myDisposed = true;
            }
        }
    }
}
