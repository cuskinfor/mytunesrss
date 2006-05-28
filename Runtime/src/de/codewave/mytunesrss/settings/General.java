/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.network.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

/**
 * General settings panel
 */
public class General {
    private static final Log LOG = LogFactory.getLog(General.class);

    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JPasswordField myPasswordInput;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
    private Settings mySettingsForm;
    private JButton myStopServerButton;
    private JLabel myServerStatusLabel;private JButton myStartServerButton;

    public JTextField getTunesXmlPathInput() {
        return myTunesXmlPathInput;
    }

    public void init(Settings settingsForm) {
        mySettingsForm = settingsForm;
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myPasswordInput.setText(MyTunesRss.CONFIG.getPassword());
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
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
        setServerStatus(MyTunesRss.BUNDLE.getString("info.server.idle"), null);
    }

    public void doStartServer() {
        int port;
        try {
            port = Integer.parseInt(myPortInput.getText().trim());
        } catch (NumberFormatException e) {
            port = MIN_PORT - 1;
        }
        final File library = new File(myTunesXmlPathInput.getText().trim());
        if (port < MIN_PORT || port > MAX_PORT) {
            SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.startServer.port"));
        } else if (!new ITunesLibraryFileFilter(false).accept(library)) {
            SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.startServer.libraryXmlFile"));
        } else if (myPasswordInput.getPassword().length == 0) {
            SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.authButNoPassword"));
        } else {
            try {
                URL libraryUrl = library.toURL();
                final Map<String, Object> contextEntries = new HashMap<String, Object>();
                mySettingsForm.updateConfigFromGui();
                contextEntries.put(MyTunesRssConfig.class.getName(), MyTunesRss.CONFIG);
                contextEntries.put(DataStore.class.getName(), MyTunesRss.STORE);
                updateDatabase(libraryUrl);
                final int serverPort = port;
                PleaseWait.start(mySettingsForm.getFrame(),
                             null,
                             MyTunesRss.BUNDLE.getString("info.server.starting"),
                             false,
                             false,
                             new PleaseWait.NoCancelTask() {
                                     public void execute() throws Exception {
                                         MyTunesRss.WEBSERVER.start(serverPort, contextEntries);
                                     }
                                 });
                if (!MyTunesRss.WEBSERVER.isRunning()) {
                    SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.WEBSERVER.getLastErrorMessage());
                } else {
                    mySettingsForm.setGuiMode(GuiMode.ServerRunning);
                    setServerRunningStatus(port);
                    if (MyTunesRss.CONFIG.isAutoUpdateDatabase()) {
                        int interval = MyTunesRss.CONFIG.getAutoUpdateDatabaseInterval();
                        MyTunesRss.DATABASE_WATCHDOG.schedule(new DatabaseWatchdogTask(interval, libraryUrl), 1000 * interval);
                    }
                }
            } catch (MalformedURLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create URL from iTunes XML file.", e);
                }
                SwingUtils.showErrorMessage(mySettingsForm.getFrame(), "Could not start server. Please check the log for errors.");
            }
        }
    }

    private void updateDatabase(final URL library) {
        final Set<Boolean> checkResult = new HashSet<Boolean>();
        PleaseWait.start(mySettingsForm.getFrame(), null, "Checking database... please wait.", false, false, new PleaseWait.NoCancelTask() {
            public void execute() {
                try {
                    checkResult.add(Boolean.valueOf(DatabaseBuilderTask.needsUpdate(library)));
                } catch (Exception e) {
                    checkResult.add(Boolean.FALSE);
                }
            }
        });
        if (checkResult.iterator().next()) {
            PleaseWait.start(mySettingsForm.getFrame(),
                             null,
                             DatabaseBuilderTask.BuildType.Update.getVerb() + " database... please wait.",
                             false,
                             false,
                             new DatabaseBuilderTask(library, DatabaseBuilderTask.BuildType.Update));
        }
    }

    private void setServerRunningStatus(int serverPort) {
        String[] localAddresses = NetworkUtils.getLocalNetworkAddresses();
        if (localAddresses.length == 0) {
            setServerStatus(MyTunesRss.BUNDLE.getString("info.server.running"), null);
        } else {
            StringBuffer tooltip = new StringBuffer("<html>").append(MyTunesRss.BUNDLE.getString("info.server.running.addressInfo"));
            for (int i = 0; i < localAddresses.length; i++) {
                tooltip.append("http://").append(localAddresses[i]).append(":").append(serverPort);
                tooltip.append(i + 1 < localAddresses.length ? "<br>" : "</html>");
            }
            setServerStatus(MyTunesRss.BUNDLE.getString("info.server.running") + " [ http://" + localAddresses[0] + ":" + serverPort + " ] ",
                                     tooltip.toString());
        }
        myRootPanel.validate();
    }

    public void doStopServer() {
        PleaseWait.start(mySettingsForm.getFrame(),
                         null,
                         MyTunesRss.BUNDLE.getString("info.server.stopping"),
                         false,
                         false,
                         new PleaseWait.NoCancelTask() {
                             public void execute() throws Exception {
                                 MyTunesRss.WEBSERVER.stop();
                             }
                         });
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            mySettingsForm.setGuiMode(GuiMode.ServerIdle);
            setServerStatus(MyTunesRss.BUNDLE.getString("info.server.idle"), null);
            myRootPanel.validate();
            if (MyTunesRss.CONFIG.isAutoUpdateDatabase()) {
                MyTunesRss.DATABASE_WATCHDOG.cancel();
                MyTunesRss.DATABASE_WATCHDOG = new Timer("MyTunesRSSDatabaseWatchdog");
            }
        } else {
            SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.WEBSERVER.getLastErrorMessage());
        }
    }

    public void updateConfigFromGui() {
        try {
            MyTunesRss.CONFIG.setPort(Integer.parseInt(myPortInput.getText().trim()));
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
        MyTunesRss.CONFIG.setLibraryXml(myTunesXmlPathInput.getText().trim());
        MyTunesRss.CONFIG.setPassword(new String(myPasswordInput.getPassword()).trim());
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myPortInput, false);
                SwingUtils.enableElementAndLabel(myPasswordInput, false);
                myStartServerButton.setEnabled(false);
                myStopServerButton.setEnabled(true);
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myPortInput, true);
                SwingUtils.enableElementAndLabel(myPasswordInput, true);
                myStartServerButton.setEnabled(true);
                myStopServerButton.setEnabled(false);
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, true);
                myTunesXmlPathLookupButton.setEnabled(true);
                break;
        }
    }

    public void setServerStatus(String text, String tooltipText) {
        if (text != null) {
            myServerStatusLabel.setText(text);
        }
        if (tooltipText != null) {
            myServerStatusLabel.setToolTipText(tooltipText);
        }
    }

    public class TunesXmlPathLookupButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new ITunesLibraryFileFilter(true));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupLibraryXml.title"));
            if (fileChooser.showDialog(myRootPanel.getTopLevelAncestor(), null) == JFileChooser.APPROVE_OPTION) {
                try {
                    myTunesXmlPathInput.setText(fileChooser.getSelectedFile().getCanonicalPath());
                } catch (IOException e) {
                    SwingUtils.showErrorMessage(mySettingsForm.getFrame(),
                                                MyTunesRss.BUNDLE.getString("error.lookupLibraryXml.failure") + e.getMessage());
                }
            }
        }
    }

    public class ITunesLibraryFileFilter extends javax.swing.filechooser.FileFilter {
        private boolean myAllowDirectories;

        public ITunesLibraryFileFilter(boolean allowDirectories) {
            myAllowDirectories = allowDirectories;
        }

        public boolean accept(File f) {
            return f != null && f.exists() &&
                    ((f.isDirectory() && myAllowDirectories) || (f.isFile() && LIBRARY_XML_FILE_NAME.equalsIgnoreCase(f.getName())));
        }

        public String getDescription() {
            return "iTunes Library";
        }
    }
}