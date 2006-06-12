package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.task.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.settings.Settings
 */
public class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private JFrame myFrame;
    private JPanel myRootPanel;
    private General myGeneralForm;
    private Options myOptionsForm;
    private Info myInfoForm;
    private JButton myStartServerButton;
    private JButton myStopServerButton;
    private JButton myQuitButton;

    public JFrame getFrame() {
        return myFrame;
    }

    public General getGeneralForm() {
        return myGeneralForm;
    }

    public Options getOptionsForm() {
        return myOptionsForm;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void init(JFrame frame) {
        myGeneralForm.init(this);
        myOptionsForm.init(this);
        myInfoForm.init(this);
        myFrame = frame;
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
        int port;
        try {
            port = Integer.parseInt(myGeneralForm.getPortInput().getText().trim());
        } catch (NumberFormatException e) {
            port = MIN_PORT - 1;
        }
        final File library = new File(myGeneralForm.getTunesXmlPathInput().getText().trim());
        if (port < MIN_PORT || port > MAX_PORT) {
            SwingUtils.showErrorMessage(getFrame(), MyTunesRss.BUNDLE.getString("error.illegalServerPort"));
        } else if (!new General.ITunesLibraryFileFilter(false).accept(library)) {
            SwingUtils.showErrorMessage(getFrame(), MyTunesRss.BUNDLE.getString("error.illegalTunesXml"));
        } else if (myGeneralForm.getPasswordInput().getPassword().length == 0) {
            SwingUtils.showErrorMessage(getFrame(), MyTunesRss.BUNDLE.getString("error.missingAuthPassword"));
        } else {
            try {
                URL libraryUrl = library.toURL();
                final Map<String, Object> contextEntries = new HashMap<String, Object>();
                updateConfigFromGui();
                contextEntries.put(MyTunesRssConfig.class.getName(), MyTunesRss.CONFIG);
                contextEntries.put(DataStore.class.getName(), MyTunesRss.STORE);
                updateDatabase(libraryUrl);
                final int serverPort = port;
                PleaseWait.start(getFrame(), null, MyTunesRss.BUNDLE.getString("pleaseWait.serverstarting"), false, false, new PleaseWait.NoCancelTask() {
                    public void execute() throws Exception {
                        MyTunesRss.WEBSERVER.start(serverPort, contextEntries);
                    }
                });
                if (!MyTunesRss.WEBSERVER.isRunning()) {
                    SwingUtils.showErrorMessage(getFrame(), MyTunesRss.WEBSERVER.getLastErrorMessage());
                } else {
                    setGuiMode(GuiMode.ServerRunning);
                    myGeneralForm.setServerRunningStatus(port);
                    if (MyTunesRss.CONFIG.isAutoUpdateDatabase()) {
                        int interval = MyTunesRss.CONFIG.getAutoUpdateDatabaseInterval();
                        MyTunesRss.DATABASE_WATCHDOG.schedule(new DatabaseWatchdogTask(myOptionsForm, interval * 60, libraryUrl), 1000 * interval);
                    }
                }
            } catch (MalformedURLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create URL from iTunes XML file.", e);
                }
                SwingUtils.showErrorMessage(getFrame(), MyTunesRss.BUNDLE.getString("error.serverStart"));
            }
        }
    }

    private void updateDatabase(final URL library) {
        PleaseWait.start(getFrame(),
                         null, MyTunesRss.BUNDLE.getString("settings.buildDatabase"),
                         false,
                         false,
                         new DatabaseBuilderTask(library, myOptionsForm));
    }

    public void doStopServer() {
        PleaseWait.start(getFrame(), null, MyTunesRss.BUNDLE.getString("pleaseWait.serverstopping"), false, false, new PleaseWait.NoCancelTask() {
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
        } else {
            SwingUtils.showErrorMessage(getFrame(), MyTunesRss.WEBSERVER.getLastErrorMessage());
        }
    }

    public void doQuitApplication() {
        if (MyTunesRss.WEBSERVER.isRunning()) {
            doStopServer();
        }
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_x", getFrame().getLocation().x);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_y", getFrame().getLocation().y);
            updateConfigFromGui();
            MyTunesRss.CONFIG.save();
            PleaseWait.start(getFrame(), null, MyTunesRss.BUNDLE.getString("pleaseWait.shutdownDatabase"), false, false, new PleaseWait.NoCancelTask() {
                public void execute() {
                    try {
                        MyTunesRss.STORE.destroy();
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not destroy the store.", e);
                        }
                    }
                }
            });
            System.exit(0);
        }
    }
}