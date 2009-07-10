/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.Settings;
import de.codewave.systray.SystrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;


/**
 * de.codewave.mytunesrss.SysTray
 */
public class MyTunesRssSystray implements MyTunesRssEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssSystray.class);

    private UUID myUUID;
    private MenuItem myQuit;
    private MenuItem myStartServer;
    private MenuItem myStopServer;
    private MenuItem myUpdate;
    private MenuItem myShow;

    public MyTunesRssSystray(Settings settingsForm) throws AWTException {
        Image image = Toolkit.getDefaultToolkit().createImage(MyTunesRss.class.getResource("/de/codewave/mytunesrss/SysTray.gif"));
        String tooltip = MyTunesRssUtils.getBundleString("systray.menuLabel");
        PopupMenu menu = createPopupMenu(settingsForm);
        myUUID = SystrayUtils.add(image, tooltip, menu);
    }

    private PopupMenu createPopupMenu(Settings settingsForm) throws AWTException {
        ActionListener menuListener = new Listener(settingsForm);
        PopupMenu menu = new PopupMenu();
        myQuit = new MenuItem(MyTunesRssUtils.getBundleString("systray.quit"));
        myQuit.setActionCommand("quit");
        myQuit.addActionListener(menuListener);
        menu.add(myQuit);
        menu.addSeparator();
        myShow = new MenuItem(MyTunesRssUtils.getBundleString("systray.show"));
        myShow.setActionCommand("show");
        myShow.addActionListener(menuListener);
        myShow.setEnabled(false);
        menu.add(myShow);
        menu.addSeparator();
        myUpdate = new MenuItem(MyTunesRssUtils.getBundleString("systray.updateDatabase"));
        myUpdate.setActionCommand("update");
        myUpdate.addActionListener(menuListener);
        menu.add(myUpdate);
        menu.addSeparator();
        myStopServer = new MenuItem(MyTunesRssUtils.getBundleString("systray.stopServer"));
        myStopServer.setActionCommand("stop_server");
        myStopServer.addActionListener(menuListener);
        myStopServer.setEnabled(false);
        menu.add(myStopServer);
        myStartServer = new MenuItem(MyTunesRssUtils.getBundleString("systray.startServer"));
        myStartServer.setActionCommand("start_server");
        myStartServer.addActionListener(menuListener);
        menu.add(myStartServer);
        return menu;
    }

    public boolean isAvailable() {
        return myUUID != null;
    }

    public UUID getUUID() {
        return myUUID;
    }

    public void setServerRunning() {
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(true);
    }

    public void setServerStopped() {
        myStartServer.setEnabled(true);
        myStopServer.setEnabled(false);
    }

    public void setDatabaseUpdateRunning() {
        myUpdate.setEnabled(false);
    }

    public void setDatabaseUpdateFinished() {
        myUpdate.setEnabled(false);
    }

    public void setMinimized() {
        myShow.setEnabled(true);
    }

    public void handleEvent(MyTunesRssEvent event) {
        switch (event) {
            case SERVER_STARTED:
                setServerRunning();
                break;
            case SERVER_STOPPED:
                setServerStopped();
                break;
            case DATABASE_UPDATE_FINISHED:
            case DATABASE_UPDATE_FINISHED_NOT_RUN:
                setDatabaseUpdateFinished();
                break;
            case DATABASE_UPDATE_STATE_CHANGED:
                setDatabaseUpdateRunning();
                break;
        }
    }

    public class Listener implements ActionListener {
        private Settings mySettingsForm;

        public Listener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        public void actionPerformed(ActionEvent e) {
            if (StringUtils.equals(e.getActionCommand(), "quit")) {
                mySettingsForm.doQuitApplication();
            } else if (StringUtils.equals(e.getActionCommand(), "show")) {
                showFrame();
            } else if (StringUtils.equals(e.getActionCommand(), "update")) {
                updateDatabase();
            } else if (StringUtils.equals(e.getActionCommand(), "start_server")) {
                mySettingsForm.doStartServer();
            } else if (StringUtils.equals(e.getActionCommand(), "stop_server")) {
                mySettingsForm.doStopServer();
            }
        }

        private void showFrame() {
            LOGGER.debug("Showing root frame from system tray.");
            if (MyTunesRss.ROOT_FRAME.getExtendedState() == JFrame.ICONIFIED) {
                MyTunesRss.ROOT_FRAME.setExtendedState(JFrame.NORMAL);
            } else {
                MyTunesRss.ROOT_FRAME.setExtendedState(MyTunesRss.ROOT_FRAME.getExtendedState() & ~JFrame.ICONIFIED);
            }
            MyTunesRss.ROOT_FRAME.setVisible(true);
            MyTunesRss.ROOT_FRAME.toFront();
            myShow.setEnabled(false);
        }

        private void updateDatabase() {
            MyTunesRssUtils.executeDatabaseUpdate();
        }
    }
}
