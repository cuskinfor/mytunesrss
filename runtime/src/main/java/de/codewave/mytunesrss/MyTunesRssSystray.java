/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.systray.SystrayUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

public class MyTunesRssSystray {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssSystray.class);

    private UUID myUUID;
    private JFrame myFrame;

    public MyTunesRssSystray(JFrame frame) throws AWTException {
        myFrame = frame;
        String resourceName = SystemUtils.IS_OS_WINDOWS ? "/de/codewave/mytunesrss/SysTrayWindows.png" : "/de/codewave/mytunesrss/SysTray.png";
        Image image = Toolkit.getDefaultToolkit().createImage(MyTunesRss.class.getResource(resourceName));
        myUUID = SystrayUtils.add(image, null, null, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LOGGER.debug("Showing root myFrame from system tray.");
                if (myFrame.getExtendedState() == JFrame.ICONIFIED) {
                    myFrame.setExtendedState(JFrame.NORMAL);
                } else {
                    myFrame.setExtendedState(myFrame.getExtendedState() & ~JFrame.ICONIFIED);
                }
                myFrame.setVisible(true);
                myFrame.toFront();
            }
        });
    }

    public boolean isAvailable() {
        return myUUID != null;
    }
}
