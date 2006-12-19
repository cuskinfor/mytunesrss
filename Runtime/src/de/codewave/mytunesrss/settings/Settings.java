package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.Timer;

/**
 * de.codewave.mytunesrss.settings.Settings
 */
public class Settings {
    private JPanel myRootPanel;
    private General myGeneralForm;
    private Directories myDirectoriesForm;
    private UserManagement myUserManagementForm;
    private Info myInfoForm;
    private JButton myStartServerButton;
    private JButton myStopServerButton;
    private JButton myQuitButton;
    private JTabbedPane myTabbedPane;

    public General getGeneralForm() {
        return myGeneralForm;
    }

    public Directories getOptionsForm() {
        return myDirectoriesForm;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void init() {
        myStartServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartServer();
            }
        });
        myStopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStopServer();
            }
        });
        myQuitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuitApplication();
            }
        });
        myGeneralForm.init();
        myDirectoriesForm.init();
        myUserManagementForm.init();
        myInfoForm.init();
        myTabbedPane.addChangeListener(new TabSwitchListener());
    }

    public void updateConfigFromGui() {
        myGeneralForm.updateConfigFromGui();
        myDirectoriesForm.updateConfigFromGui();
        myInfoForm.updateConfigFromGui();
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerRunning();
                }
                myStartServerButton.setEnabled(false);
                myStopServerButton.setEnabled(true);
                break;
            case ServerIdle:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerStopped();
                }
                myStartServerButton.setEnabled(true);
                myStopServerButton.setEnabled(false);
        }
        myGeneralForm.setGuiMode(mode);
        myDirectoriesForm.setGuiMode(mode);
        myUserManagementForm.setGuiMode(mode);
    }

    public void doStartServer() {
        updateConfigFromGui();
        MyTunesRss.startWebserver();
        if (MyTunesRss.WEBSERVER.isRunning()) {
            setGuiMode(GuiMode.ServerRunning);
            myGeneralForm.setServerRunningStatus(MyTunesRss.CONFIG.getPort());
        }
    }

    public void doStopServer() {
        MyTunesRss.stopWebserver();
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            setGuiMode(GuiMode.ServerIdle);
            myGeneralForm.setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
            if (MyTunesRss.CONFIG.isAutoUpdateDatabase()) {
                MyTunesRss.SERVER_RUNNING_TIMER.cancel();
                MyTunesRss.SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
            }
        }
    }

    public void doQuitApplication() {
        updateConfigFromGui();
        MyTunesRssUtils.shutdownGracefully();
    }

    public class TabSwitchListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateConfigFromGui();
        }
    }

}