/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.Settings;
import de.codewave.systray.SystrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;


/**
 * de.codewave.mytunesrss.SysTray
 */
public class MyTunesRssSystray {

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
        menu.add(myShow);
        menu.addSeparator();
        myUpdate = new MenuItem(MyTunesRssUtils
                .getBundleString("systray.updateDatabase"));
        myUpdate.setActionCommand("update");
        myUpdate.addActionListener(menuListener);
        menu.add(myUpdate);
        menu.addSeparator();
        myStopServer = new MenuItem(MyTunesRssUtils
                .getBundleString("systray.stopServer"));
        myStopServer.setActionCommand("stop_server");
        myStopServer.addActionListener(menuListener);
        menu.add(myStopServer);
        myStartServer = new MenuItem(MyTunesRssUtils
                .getBundleString("systray.startServer"));
        myStartServer.setActionCommand("start_server");
        myStartServer.addActionListener(menuListener);
        menu.add(myStartServer);
        return menu;
    }

    public UUID getUUID() {
        return myUUID;
    }

    public void disableAll() {
        myQuit.setEnabled(false);
        myShow.setEnabled(false);
        myUpdate.setEnabled(false);
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(false);
    }

    public void setServerRunning() {
        myQuit.setEnabled(true);
        myShow.setEnabled(true);
        myUpdate.setEnabled(true);
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(true);
    }

    public void setServerStopped() {
        myQuit.setEnabled(true);
        myShow.setEnabled(true);
        myUpdate.setEnabled(true);
        myStartServer.setEnabled(true);
        myStopServer.setEnabled(false);
    }

    public static class Listener implements ActionListener {
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
            MyTunesRss.ROOT_FRAME.setVisible(true);
            MyTunesRss.ROOT_FRAME.setExtendedState(JFrame.NORMAL);
        }

        private void updateDatabase() {
            MyTunesRssUtils.executeDatabaseUpdate();
        }
    }
}
