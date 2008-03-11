/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.Settings;
import snoozesoft.systray4j.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.SysTray
 */
public class SysTray {
    private SysTrayMenu myMenu;
    private SysTrayMenuItem myQuit;
    private SysTrayMenuItem myShow;
    private SysTrayMenuItem myUpdate;
    private SysTrayMenuItem myStartServer;
    private SysTrayMenuItem myStopServer;

    public SysTray(Settings settingsForm) {
        SysTrayMenuIcon menuIcon = new SysTrayMenuIcon(getClass().getResource("SysTray.ico"));
        Listener menuListener = new Listener(settingsForm);
        menuIcon.addSysTrayMenuListener(menuListener);
        myMenu = new SysTrayMenu(menuIcon, MyTunesRssUtils.getBundleString("systray.menuLabel"));
        myQuit = new SysTrayMenuItem(MyTunesRssUtils.getBundleString("systray.quit"), "quit");
        myQuit.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myQuit);
        myMenu.addSeparator();
        myShow = new SysTrayMenuItem(MyTunesRssUtils.getBundleString("systray.show"), "show");
        myShow.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myShow);
        myMenu.addSeparator();
        myUpdate = new SysTrayMenuItem(MyTunesRssUtils.getBundleString("systray.updateDatabase"), "update");
        myUpdate.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myUpdate);
        myMenu.addSeparator();
        myStopServer = new SysTrayMenuItem(MyTunesRssUtils.getBundleString("systray.stopServer"), "stop_server");
        myStopServer.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myStopServer);
        myStartServer = new SysTrayMenuItem(MyTunesRssUtils.getBundleString("systray.startServer"), "start_server");
        myStartServer.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myStartServer);
        hide();
        setServerStopped();
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
            } else if ("update".equals(sysTrayMenuEvent.getActionCommand())) {
                updateDatabase();
            } else if ("stop_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doStopServer();
            } else if ("start_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doStartServer();
            }
        }

        private void updateDatabase() {
            MyTunesRssUtils.executeDatabaseUpdate();
        }
    }
}
