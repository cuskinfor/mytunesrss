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
    private Server myServerForm;
    private Directories myDirectoriesForm;
    private UserManagement myUserManagementForm;
    private Misc myMiscForm;
    private JButton myStartServerButton;
    private JButton myStopServerButton;
    private JButton myQuitButton;
    private JTabbedPane myTabbedPane;
    private Database myDatabaseForm;
    private Info myInfoForm;
    private Addons myAddonsForm;
    private JPanel myAddonsPanel;
    private Streaming myStreamingForm;
    private JPanel myStreamingPanel;

    public Database getDatabaseForm() {
        return myDatabaseForm;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void init() {
        initRegistration();
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
        myServerForm.init();
        myDatabaseForm.init();
        myDirectoriesForm.init();
        myMiscForm.init();
        myInfoForm.init();
        myUserManagementForm.init();
        myStreamingForm.init();
        myAddonsForm.init();
        myTabbedPane.addChangeListener(new TabSwitchListener());
    }

    private void initRegistration() {
        if (!MyTunesRss.REGISTRATION.isRegistered()) {
            myTabbedPane.remove(myAddonsPanel);
            myTabbedPane.remove(myStreamingPanel);
        }
    }

    public String updateConfigFromGui() {
        StringBuffer messages = new StringBuffer();
        String message = myServerForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myDatabaseForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myDirectoriesForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myMiscForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myStreamingForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        message = myAddonsForm.updateConfigFromGui();
        if (message != null) {
            messages.append(message).append(" ");
        }
        String returnValue = messages.toString().trim();
        return returnValue.length() > 0 ? returnValue : null;
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
        myServerForm.setGuiMode(mode);
        myDatabaseForm.setGuiMode(mode);
        myDirectoriesForm.setGuiMode(mode);
        myMiscForm.setGuiMode(mode);
        myStreamingForm.setGuiMode(mode);
        myAddonsForm.setGuiMode(mode);
    }

    public void doStartServer() {
        String messages = updateConfigFromGui();
        if (messages == null) {
            MyTunesRss.startWebserver();
            if (MyTunesRss.WEBSERVER.isRunning()) {
                setGuiMode(GuiMode.ServerRunning);
                myServerForm.setServerRunningStatus(MyTunesRss.CONFIG.getPort());
            }
        } else {
            MyTunesRssUtils.showErrorMessage(messages);
        }
    }

    public void doStopServer() {
        MyTunesRss.stopWebserver();
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            setGuiMode(GuiMode.ServerIdle);
            myServerForm.setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
            if (MyTunesRss.CONFIG.isAutoUpdateDatabase()) {
                MyTunesRss.SERVER_RUNNING_TIMER.cancel();
                MyTunesRss.SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
            }
        }
    }

    public void doQuitApplication() {
        String messages = updateConfigFromGui();
        if (messages == null) {
            MyTunesRssUtils.shutdownGracefully();
        } else {
            MyTunesRssUtils.showErrorMessage(messages);
        }
    }

    public class TabSwitchListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateConfigFromGui();
        }
    }

}