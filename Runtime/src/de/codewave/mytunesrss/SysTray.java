/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
import snoozesoft.systray4j.*;

import javax.swing.*;
import java.io.*;
import java.net.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.SysTray
 */
public class SysTray {
    private static final Log LOG = LogFactory.getLog(SysTray.class);

    private SysTrayMenu myMenu;
    private SysTrayMenuItem myQuit;
    private SysTrayMenuItem myShow;
    private SysTrayMenuItem myUpdateDatabase;
    private SysTrayMenuItem myStartServer;
    private SysTrayMenuItem myStopServer;

    public SysTray(Settings settingsForm) {
        SysTrayMenuIcon menuIcon = new SysTrayMenuIcon(getClass().getResource("SysTray.ico"));
        Listener menuListener = new Listener(settingsForm);
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

        public Listener(Settings settingsForm) {
            mySettingsForm = settingsForm;
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
                mySettingsForm.doQuitApplication();
            } else if ("show".equals(sysTrayMenuEvent.getActionCommand())) {
                showFrame();
            } else if ("update_database".equals(sysTrayMenuEvent.getActionCommand())) {
                try {
                    PleaseWait.start(mySettingsForm.getFrame(), null, MyTunesRss.BUNDLE.getString("settings.buildDatabase"), false, false, new DatabaseBuilderTask(new File(mySettingsForm.getGeneralForm().getTunesXmlPathInput().getText()).toURL(), mySettingsForm.getOptionsForm()));
                } catch (MalformedURLException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not build database.", e1);
                    }

                }
            } else if ("stop_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doStopServer();
            } else if ("start_server".equals(sysTrayMenuEvent.getActionCommand())) {
                mySettingsForm.doStartServer();
            }
        }
    }

}