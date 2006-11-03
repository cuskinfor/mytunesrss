/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
import org.apache.commons.logging.*;
import snoozesoft.systray4j.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.SysTray
 */
public class SysTray {
    private static final Log LOG = LogFactory.getLog(SysTray.class);

    private SysTrayMenu myMenu;
    private SysTrayMenuItem myQuit;
    private SysTrayMenuItem myShow;
    private SysTrayMenuItem myStartServer;
    private SysTrayMenuItem myStopServer;

    public SysTray(Settings settingsForm) {
        SysTrayMenuIcon menuIcon = new SysTrayMenuIcon(getClass().getResource("SysTray.ico"));
        Listener menuListener = new Listener(settingsForm);
        menuIcon.addSysTrayMenuListener(menuListener);
        myMenu = new SysTrayMenu(menuIcon, MyTunesRss.BUNDLE.getString("systray.menuLabel"));
        myQuit = new SysTrayMenuItem(MyTunesRss.BUNDLE.getString("systray.quit"), "quit");
        myQuit.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myQuit);
        myMenu.addSeparator();
        myShow = new SysTrayMenuItem(MyTunesRss.BUNDLE.getString("systray.show"), "show");
        myShow.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myShow);
        myMenu.addSeparator();
        myMenu.addSeparator();
        myStopServer = new SysTrayMenuItem(MyTunesRss.BUNDLE.getString("systray.stopServer"), "stop_server");
        myStopServer.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myStopServer);
        myStartServer = new SysTrayMenuItem(MyTunesRss.BUNDLE.getString("systray.startServer"), "start_server");
        myStartServer.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myStartServer);
        hide();
        setServerStopped();
    }

    public void disableAll() {
        myQuit.setEnabled(false);
        myShow.setEnabled(false);
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(false);
    }

    public void setServerRunning() {
        myQuit.setEnabled(true);
        myShow.setEnabled(true);
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(true);
    }

    public void setServerStopped() {
        myQuit.setEnabled(true);
        myShow.setEnabled(true);
        myStartServer.setEnabled(true);
        myStopServer.setEnabled(false);
    }

    public void show() {
        myMenu.showIcon();
    }

    public void hide() {
        myMenu.hideIcon();
    }

    public static class Listener extends SysTrayMenuAdapter {
        private Settings mySettingsForm;

        public Listener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        @Override
        public void iconLeftDoubleClicked(SysTrayMenuEvent sysTrayMenuEvent) {
            showFrame();
        }

        private void showFrame() {
            MyTunesRss.ROOT_FRAME.setVisible(true);
            MyTunesRss.ROOT_FRAME.setExtendedState(JFrame.NORMAL);
            MyTunesRss.SYSTRAYMENU.hide();
        }

        @Override
        public void menuItemSelected(SysTrayMenuEvent sysTrayMenuEvent) {
            if ("quit".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doQuitApplication();
            } else if ("show".equals(sysTrayMenuEvent.getActionCommand())) {
                showFrame();
            } else if ("stop_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doStopServer();
            } else if ("start_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doStartServer();
            }
        }
    }

}