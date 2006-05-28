/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
import snoozesoft.systray4j.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * de.codewave.mytunesrss.MyTunesRSSSysTrayMenu
 */
public class MyTunesRSSSysTrayMenu {
    private SysTrayMenu myMenu;
    private SysTrayMenuItem myQuit;
    private SysTrayMenuItem myShow;
    private SysTrayMenuItem myUpdateDatabase;
    private SysTrayMenuItem myStartServer;
    private SysTrayMenuItem myStopServer;

    public MyTunesRSSSysTrayMenu(Settings settingsForm, WindowListener windowListener) {
        SysTrayMenuIcon menuIcon = new SysTrayMenuIcon(MyTunesRss.class.getResource("MyTunesRSS.ico"));
        Listener menuListener = new Listener(settingsForm, windowListener);
        menuIcon.addSysTrayMenuListener(menuListener);
        myMenu = new SysTrayMenu(menuIcon, "This is MyTunesRSS");
        myQuit = new SysTrayMenuItem("Quit MyTunesRSS", "quit");
        myQuit.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myQuit);
        myMenu.addSeparator();
        myShow = new SysTrayMenuItem("Show MyTunesRSS", "show");
        myShow.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myShow);
        myMenu.addSeparator();
        myUpdateDatabase = new SysTrayMenuItem("Update database", "update_database");
        myUpdateDatabase.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myUpdateDatabase);
        myMenu.addSeparator();
        myStopServer = new SysTrayMenuItem("Stop server", "stop_server");
        myStopServer.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myStopServer);
        myStartServer = new SysTrayMenuItem("Start server", "start_server");
        myStartServer.addSysTrayMenuListener(menuListener);
        myMenu.addItem(myStartServer);
        hide();
        setServerStopped();
    }

    public void disableAll() {
        myQuit.setEnabled(false);
        myShow.setEnabled(false);
        myUpdateDatabase.setEnabled(false);
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(false);
    }

    public void setServerRunning() {
        myQuit.setEnabled(true);
        myShow.setEnabled(true);
        myUpdateDatabase.setEnabled(true);
        myStartServer.setEnabled(false);
        myStopServer.setEnabled(true);
    }

    public void setServerStopped() {
        myQuit.setEnabled(true);
        myShow.setEnabled(true);
        myUpdateDatabase.setEnabled(true);
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
        private WindowListener myWindowListener;

        public Listener(Settings settingsForm, WindowListener windowListener) {
            mySettingsForm = settingsForm;
            myWindowListener = windowListener;
        }

        @Override
        public void iconLeftDoubleClicked(SysTrayMenuEvent sysTrayMenuEvent) {
            showFrame();
        }

        private void showFrame() {
            mySettingsForm.getFrame().setVisible(true);
            mySettingsForm.getFrame().setExtendedState(JFrame.NORMAL);
            MyTunesRss.SYSTRAYMENU.hide();
        }

        @Override
        public void menuItemSelected(SysTrayMenuEvent sysTrayMenuEvent) {
            if ("quit".equals(sysTrayMenuEvent.getActionCommand())) {
                myWindowListener.windowClosing(null);
            } else if ("show".equals(sysTrayMenuEvent.getActionCommand())) {
                showFrame();
            } else if ("update_database".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.getOptionsForm().runBuildDatabaseTask(DatabaseBuilderTask.BuildType.Update);
            } else if ("stop_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.getGeneralForm().doStopServer();
            } else if ("start_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.getGeneralForm().doStartServer();
            }
        }
    }

}