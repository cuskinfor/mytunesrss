package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ModelInfoDialog {

    private JDialog info;

    public ModelInfoDialog(String message) {
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
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    info.setVisible(true);
                }
            }, "MyTunesRSSModalInfoDialog");
            thread.start();
            info.setLocationRelativeTo(null);
        }
    }

    public void destroy() {
        if (!MyTunesRssUtils.isHeadless()) {
            info.dispose();
        }
    }
}
