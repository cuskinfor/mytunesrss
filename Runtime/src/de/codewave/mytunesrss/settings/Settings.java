package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.settings.Settings
 */
public class Settings {
    private JPanel myRootPanel;
    private General myGeneralForm;
    private Options myOptionsForm;
    private Info myInfoForm;
    private JButton myStartServerButton;
    private JButton myStopServerButton;
    private JButton myQuitButton;

    public General getGeneralForm() {
        return myGeneralForm;
    }

    public Options getOptionsForm() {
        return myOptionsForm;
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
        myOptionsForm.init();
        myInfoForm.init();
    }

    public void updateConfigFromGui() {
        myGeneralForm.updateConfigFromGui();
        myOptionsForm.updateConfigFromGui();
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
        myOptionsForm.setGuiMode(mode);
        myRootPanel.validate();
    }

    public void doStartServer() {
        updateConfigFromGui();
        DatabaseBuilderTask databaseBuilderTask = new GuiDatabaseBuilderTask(myOptionsForm);
        MyTunesRss.startWebserver(databaseBuilderTask);
        if (MyTunesRss.WEBSERVER.isRunning()) {
            setGuiMode(GuiMode.ServerRunning);
            myGeneralForm.setServerRunningStatus(MyTunesRss.CONFIG.getPort());
        }
    }

    public void doStopServer() {
        MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.serverstopping"), null, false, new MyTunesRssTask() {
            public void execute() throws Exception {
                MyTunesRss.WEBSERVER.stop();
            }
        });
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            setGuiMode(GuiMode.ServerIdle);
            myGeneralForm.setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
            myRootPanel.validate();
            if (MyTunesRss.CONFIG.isAutoUpdateDatabase()) {
                MyTunesRss.DATABASE_WATCHDOG.cancel();
                MyTunesRss.DATABASE_WATCHDOG = new Timer("MyTunesRSSDatabaseWatchdog");
            }
        }
    }

    public void doQuitApplication() {
        if (MyTunesRss.WEBSERVER.isRunning()) {
            doStopServer();
        }
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_x", MyTunesRss.ROOT_FRAME.getLocation().x);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_y", MyTunesRss.ROOT_FRAME.getLocation().y);
            updateConfigFromGui();
            MyTunesRss.CONFIG.save();
            MyTunesRss.DATABASE_WATCHDOG.cancel();
            MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.shutdownDatabase"), null, false, new MyTunesRssTask() {
                public void execute() {
                    MyTunesRss.STORE.destroy();
                }
            });
            MyTunesRss.ROOT_FRAME.dispose();
            System.exit(0);
        }
    }
}