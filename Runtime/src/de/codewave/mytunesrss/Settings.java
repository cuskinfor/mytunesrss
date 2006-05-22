package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.utils.*;
import de.codewave.utils.network.*;
import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.catalina.session.*;
import org.apache.catalina.startup.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.Settings
 */
public class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";

    private final ResourceBundle myMainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");

    private JFrame myFrame;
    private JTextField myPort;
    private JTextField myTunesXmlPath;
    private JPanel myRootPanel;
    private JLabel myStatusText;
    private JButton myStartStopButton;
    private JButton myQuitButton;
    private JButton myLookupButton;
    private JPasswordField myPassword;
    private JButton myShowLogButton;
    private JLabel myMaxMemLabel;
    private JSpinner myMaxMemSpinner;
    private JButton myMaxMemSaveButton;
    private JCheckBox myWriteLogCheckbox;
    private JCheckBox myUpdateOnStartCheckbox;
    private JButton myUpdateButton;
    private JButton myRebuildDatabase;
    private JCheckBox myAutoStartServer;
    private Embedded myServer;
    private LogDisplay myLogDisplay = new LogDisplay();
    private boolean myRememberedUpdateOnStart;

    public Settings(final JFrame frame) throws UnsupportedEncodingException {
        Logger.getRootLogger().removeAllAppenders();
        Logger.getLogger("de.codewave").removeAllAppenders();
        Logger.getRootLogger().addAppender(myLogDisplay);
        Logger.getLogger("de.codewave").addAppender(myLogDisplay);
        myFrame = frame;
        setStatus(myMainBundle.getString("info.server.idle"));
        MyTunesRssConfig data = new MyTunesRssConfig();
        data.load();
        myPort.setText(data.getPort());
        myTunesXmlPath.setText(data.getLibraryXml());
        myPassword.setText(data.getPassword());
        myWriteLogCheckbox.setSelected(data.isLoggingEnabled());
        myLogDisplay.setLoggingEnabled(data.isLoggingEnabled());
        if (!myLogDisplay.isLoggingEnabled()) {
            myShowLogButton.setEnabled(false);
        }
        int minMemory = ProgramUtils.getMemorySwitch(MemorySwitchType.Minimum);
        int maxMemory = ProgramUtils.getMemorySwitch(MemorySwitchType.Maxmimum);
        if (maxMemory != -1) {
            SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(maxMemory, Math.max(10, minMemory), 500, 10);
            myMaxMemSpinner.setModel(spinnerNumberModel);
        } else {
            myMaxMemLabel.setVisible(false);
            myMaxMemSpinner.setVisible(false);
            myMaxMemSaveButton.setVisible(false);
        }
        myUpdateOnStartCheckbox.setSelected(data.isCheckUpdateOnStart());
        myAutoStartServer.setSelected(data.isAutoStartServer());
        if (myAutoStartServer.isSelected()) {
            myUpdateOnStartCheckbox.setSelected(false);
            myUpdateOnStartCheckbox.setEnabled(false);
        }
        enableConfig(true);
        myStartStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartStopServer();
            }
        });
        myQuitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuitApplication();
            }
        });
        myLookupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLookupLibraryFile();
            }
        });
        myShowLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myShowLogButton.setEnabled(false);
                myLogDisplay.show(frame, myShowLogButton);
            }
        });
        myWriteLogCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (myWriteLogCheckbox.isSelected()) {
                    myShowLogButton.setEnabled(true);
                    myLogDisplay.setLoggingEnabled(true);
                } else {
                    myShowLogButton.setEnabled(false);
                    myLogDisplay.setLoggingEnabled(false);
                }
            }
        });
        myMaxMemSaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int maxMem = ((Integer)myMaxMemSpinner.getValue()).intValue();
                if (ProgramUtils.updateMemorySwitch(MemorySwitchType.Maxmimum, maxMem)) {
                    showInfoMessage(myMainBundle.getString("info.savemem.success"));
                } else {
                    showErrorMessage(myMainBundle.getString("error.memsave.failure"));
                }
            }
        });
        myUpdateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        checkForUpdate(false);
                    }
                });
            }
        });
        myRebuildDatabase.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    PleaseWait.start(myFrame, null, "Rebuilding database... please wait.", false, false, new DatabaseBuilderTask(new File(
                            myTunesXmlPath.getText()).toURL()));
                } catch (MalformedURLException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(null, e1);
                    }

                }
            }
        });
        myAutoStartServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (myAutoStartServer.isSelected()) {
                    myRememberedUpdateOnStart = myUpdateOnStartCheckbox.isSelected();
                    myUpdateOnStartCheckbox.setSelected(false);
                    myUpdateOnStartCheckbox.setEnabled(false);
                } else {
                    myUpdateOnStartCheckbox.setSelected(myRememberedUpdateOnStart);
                    myUpdateOnStartCheckbox.setEnabled(true);
                }
            }
        });
    }

    public boolean isUpdateCheckOnStartup() {
        return myUpdateOnStartCheckbox.isSelected();
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void doLookupLibraryFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ITunesLibraryFileFilter(true));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(myMainBundle.getString("dialog.lookupLibraryXml.title"));
        if (fileChooser.showDialog(getRootPanel().getTopLevelAncestor(), null) == JFileChooser.APPROVE_OPTION) {
            try {
                myTunesXmlPath.setText(fileChooser.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
                showErrorMessage(myMainBundle.getString("error.lookupLibraryXml.failure") + e.getMessage());
            }
        }
    }

    public void doStartStopServer() {
        if (myServer == null) {
            doStartServer();
        } else {
            doStopServer();
        }
    }

    public void doStartServer() {
        int port;
        try {
            port = Integer.parseInt(myPort.getText().trim());
        } catch (NumberFormatException e) {
            port = MIN_PORT - 1;
        }
        final int serverPort = port;
        final File library = new File(myTunesXmlPath.getText().trim());
        if (port < MIN_PORT || port > MAX_PORT) {
            showErrorMessage(myMainBundle.getString("error.startServer.port"));
        } else if (!new ITunesLibraryFileFilter(false).accept(library)) {
            showErrorMessage(myMainBundle.getString("error.startServer.libraryXmlFile"));
        } else if (myPassword.getPassword().length == 0) {
            showErrorMessage(myMainBundle.getString("error.authButNoPassword"));
        } else {
            enableButtons(false);
            enableConfig(false);
            myRootPanel.validate();
            try {
                if (DatabaseBuilderTask.needsUpdate(library.toURL())) {
                    PleaseWait.start(myFrame, null, "Rebuilding database... please wait.", false, false, new DatabaseBuilderTask(new File(
                            myTunesXmlPath.getText()).toURL()));
                }
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not update database.", e);
                }
            } catch (MalformedURLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create URL from iTunes XML file.", e);
                }
            }
            PleaseWait.start(myFrame, null, myMainBundle.getString("info.server.starting"), false, false, new PleaseWait.Task() {
                public void execute() {
                    try {
                        myServer = createServer("mytunesrss", null, serverPort, new File("."), "ROOT", "");
                        if (myServer != null) {
                            myServer.start();
                            byte health = checkServerHealth(serverPort);
                            if (health == CheckHealthResult.OK) {
                                myStartStopButton.setText(myMainBundle.getString("gui.settings.button.stopServer"));
                                myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.stopServer"));
                                setServerRunningStatus(serverPort);
                            } else {
                                myServer.stop();
                                myServer = null;
                                if (myLogDisplay.isOutOfMemoryError()) {
                                    showErrorMessage(myMainBundle.getString("error.server.startFailureOutOfMemory"));
                                } else if (health == CheckHealthResult.EMPTY_LIBRARY) {
                                    showErrorMessage(myMainBundle.getString("error.server.startFailureEmptyLibrary"));
                                } else {
                                    showErrorMessage(myMainBundle.getString("error.server.startFailureHealth"));
                                }
                                setStatus(myMainBundle.getString("info.server.idle"));
                                enableConfig(true);
                            }
                        } else {
                            showErrorMessage(myMainBundle.getString("error.server.startFailureHealth"));
                            setStatus(myMainBundle.getString("info.server.idle"));
                            enableConfig(true);
                        }
                    } catch (LifecycleException e) {
                        if (e.getMessage().contains("BindException")) {
                            showErrorMessage(myMainBundle.getString("error.server.startFailureBindException"));
                        } else {
                            showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        }
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig(true);
                    } catch (IOException e) {
                        showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig(true);
                    } catch (SQLException e) {
                        showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig(true);
                    }
                }

                protected void cancel() {
                    // intentionally left blank
                }
            });
            enableButtons(true);
            myRootPanel.validate();
        }
    }

    private void setServerRunningStatus(int serverPort) {
        String[] localAddresses = NetworkUtils.getLocalNetworkAddresses();
        if (localAddresses.length == 0) {
            setStatus(myMainBundle.getString("info.server.running"));
        } else {
            setStatus(myMainBundle.getString("info.server.running") + " [ http://" + localAddresses[0] + ":" + serverPort + " ] ");
            StringBuffer tooltip = new StringBuffer("<html>").append(myMainBundle.getString("info.server.running.addressInfo"));
            for (int i = 0; i < localAddresses.length; i++) {
                tooltip.append("http://").append(localAddresses[i]).append(":").append(serverPort);
                tooltip.append(i + 1 < localAddresses.length ? "<br>" : "</html>");
            }
            myStatusText.setToolTipText(tooltip.toString());
        }
    }

    private byte checkServerHealth(int port) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL("http://127.0.0.1:" + port + "/mytunesrss/checkHealth");
            if (LOG.isInfoEnabled()) {
                LOG.info("Trying server health URL \"" + targetUrl.toExternalForm() + "\".");
            }
            connection = (HttpURLConnection)targetUrl.openConnection();
            int responseCode = connection.getResponseCode();
            if (LOG.isInfoEnabled()) {
                LOG.info("HTTP response code is " + responseCode);
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                int result = -1;
                int trial = 0;
                while (result == -1 && trial < 10) {
                    result = inputStream.read();
                    trial++;
                    if (result == -1) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // intentionally left blank
                        }
                    }
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Health servlet response code is " + result + " after " + trial + " trials.");
                }
                return result != -1 ? (byte)result : CheckHealthResult.EOF;
            } else {
                return CheckHealthResult.INVALID_HTTP_RESPONSE;
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get a proper server health status.", e);
            }
            return CheckHealthResult.SERVER_COMMUNICATION_FAILURE;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Embedded createServer(String name, InetAddress listenAddress, int listenPort, File catalinaBasePath, String webAppName,
            String webAppContext) throws IOException, SQLException {
        MyTunesRssConfig config = createPrefDataFromGUI();
        Embedded server = new Embedded();
        server.setCatalinaBase(catalinaBasePath.getCanonicalPath());
        Engine engine = server.createEngine();
        engine.setName("engine." + name);
        engine.setDefaultHost("host." + name);
        Host host = server.createHost("host." + name, new File(catalinaBasePath, "webapps").getCanonicalPath());
        engine.addChild(host);
        Context context = server.createContext(webAppContext, webAppName);
        StandardManager sessionManager = new StandardManager();
        sessionManager.setPathname("");
        context.setManager(sessionManager);
        host.addChild(context);
        server.addEngine(engine);
        Connector connector = server.createConnector(listenAddress, listenPort, false);
        connector.setURIEncoding("UTF-8");
        server.addConnector(connector);
        context.getServletContext().setAttribute(MyTunesRssConfig.class.getName(), config);
        context.getServletContext().setAttribute(DataStore.class.getName(), MyTunesRss.STORE);
        return server;
    }

    public void doStopServer() {
        enableButtons(false);
        myRootPanel.validate();
        PleaseWait.start(myFrame, null, myMainBundle.getString("info.server.stopping"), false, false, new PleaseWait.Task() {
            public void execute() {
                try {
                    myServer.stop();
                    myServer = null;
                    setStatus(myMainBundle.getString("info.server.idle"));
                    myStatusText.setToolTipText(null);
                    enableConfig(true);
                    myStartStopButton.setText(myMainBundle.getString("gui.settings.button.startServer"));
                    myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.startServer"));
                } catch (LifecycleException e) {
                    showErrorMessage(myMainBundle.getString("error.server.stopFailure") + e.getMessage());
                    setServerRunningStatus(Integer.parseInt(myPort.getText()));
                }
            }

            protected void cancel() {
                // intentionally left blank
            }
        });
        enableButtons(true);
        myRootPanel.validate();
    }

    private void setStatus(String text) {
        myStatusText.setText(text);
    }

    public void doQuitApplication() {
        if (myQuitButton.isEnabled()) {
            if (myServer != null) {
                doStopServer();
            }
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_x", myFrame.getLocation().x);
            Preferences.userRoot().node("/de/codewave/mytunesrss").putInt("window_y", myFrame.getLocation().y);
            MyTunesRssConfig config = createPrefDataFromGUI();
            config.save();
            PleaseWait.start(myFrame, null, "Shutting down database... please wait.", false, false, new PleaseWait.Task() {
                public void execute() {
                    try {
                        MyTunesRss.STORE.destroy();
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not destroy the store.", e);
                        }
                    }
                }

                protected void cancel() {
                    // intentionally left blank
                }
            });
            System.exit(0);
        } else {
            showErrorMessage(myMainBundle.getString("error.quitWhileStartingOrStopping"));
        }
    }

    private MyTunesRssConfig createPrefDataFromGUI() {
        MyTunesRssConfig config = new MyTunesRssConfig();
        config.setPort(myPort.getText().trim());
        config.setLibraryXml(myTunesXmlPath.getText().trim());
        config.setPassword(new String(myPassword.getPassword()).trim());
        config.setLoggingEnabled(myWriteLogCheckbox.isSelected());
        config.setCheckUpdateOnStart(myUpdateOnStartCheckbox.isSelected());
        config.setAutoStartServer(myAutoStartServer.isSelected());
        return config;
    }

    private void showErrorMessage(String message) {
        showMessage(JOptionPane.ERROR_MESSAGE, myMainBundle.getString("error.title"), message);
    }

    private void showMessage(int type, String title, String message) {
        JOptionPane pane = new JOptionPane() {
            @Override
            public int getMaxCharactersPerLineCount() {
                return 100;
            }
        };
        pane.setMessageType(type);
        pane.setMessage(message);
        JDialog dialog = pane.createDialog(myFrame, title);
        dialog.setVisible(true);
    }

    private void showInfoMessage(String message) {
        showMessage(JOptionPane.INFORMATION_MESSAGE, myMainBundle.getString("info.title"), message);
    }

    private void enableButtons(boolean enabled) {
        myStartStopButton.setEnabled(enabled);
        myQuitButton.setEnabled(enabled);
    }

    private void enableConfig(boolean enabled) {
        myLookupButton.setEnabled(enabled);
        enableElementAndLabel(myPort, enabled);
        enableElementAndLabel(myTunesXmlPath, enabled);
        enableElementAndLabel(myPassword, enabled);
        myMaxMemSaveButton.setEnabled(enabled);
        enableElementAndLabel(myMaxMemSpinner, enabled);
        myUpdateOnStartCheckbox.setEnabled(enabled && !myAutoStartServer.isSelected());
        myUpdateButton.setEnabled(enabled);
        myAutoStartServer.setEnabled(enabled);
        myRebuildDatabase.setEnabled(enabled);
    }

    private void enableElementAndLabel(JComponent element, boolean enabled) {
        element.setEnabled(enabled);
        Component[] components = element.getParent().getComponents();
        if (components != null && components.length > 0) {
            for (int i = 0; i < components.length; i++) {
                if (components[i]instanceof JLabel && ((JLabel)components[i]).getLabelFor() == element) {
                    components[i].setEnabled(enabled);
                }
            }
        }
    }

    public void checkForUpdate(boolean autoCheck) {
        final UpdateInfo updateInfo = NetworkUtils.getCurrentUpdateInfo(MyTunesRss.UPDATE_URLS);
        if (updateInfo != null) {
            String noNagVersion = Preferences.userRoot().node("/de/codewave/mytunesrss").get("noNagVersion", MyTunesRss.VERSION);
            if (!updateInfo.getVersion().equals(MyTunesRss.VERSION) && (!autoCheck || !noNagVersion.equals(updateInfo.getVersion()))) {
                if (askForUpdate(updateInfo, autoCheck)) {
                    final JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setSelectedFile(new File(updateInfo.getFileName()));
                    if (fileChooser.showSaveDialog(myFrame) == JFileChooser.APPROVE_OPTION) {
                        downloadUpdate(updateInfo.getUrl(), fileChooser.getSelectedFile(), updateInfo.getVersion());
                    }
                }
            } else if (!autoCheck) {
                showInfoMessage(myMainBundle.getString("info.noUpdateAvailable"));
            }
        } else if (!autoCheck) {
            showErrorMessage(myMainBundle.getString("error.noUpdateInfo"));
        }
    }

    private void downloadUpdate(final URL url, final File file, String version) {
        PleaseWait.Task task = new PleaseWait.Task() {
            Downloader myDownloader = NetworkUtils.createDownloader(url, file, new DownloadProgressListener() {
                public void reportProgress(int progress) {
                    setPercentage(progress);
                }
            });

            public void execute() {
                switch (myDownloader.download()) {
                    case Finished:
                        showInfoMessage(myMainBundle.getString("info.updateDownloadComplete"));
                        break;
                    case Cancelled:
                        if (file.exists() && file.isFile()) {
                            file.delete();
                        }
                        showErrorMessage(myMainBundle.getString("error.updateDownloadCancelled"));
                        break;
                    case Failed:
                        if (file.exists() && file.isFile()) {
                            file.delete();
                        }
                        showErrorMessage(myMainBundle.getString("error.updateDownloadFailed"));
                        break;
                }
            }

            protected void cancel() {
                myDownloader.cancel();
            }
        };
        PleaseWait.start(myFrame, myMainBundle.getString("gui.download.title"), MessageFormat.format(myMainBundle.getString("gui.download.message"),
                                                                                                     version), true, true, task);
    }

    private boolean askForUpdate(UpdateInfo updateInfo, boolean autoCheck) {
        JOptionPane pane = new JOptionPane() {
            @Override
            public int getMaxCharactersPerLineCount() {
                return 100;
            }
        };
        pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        pane.setMessage(MessageFormat.format(myMainBundle.getString("info.newVersionAvailable.message"),
                                             MyTunesRss.VERSION,
                                             updateInfo.getVersion()));
        String stopNagging = myMainBundle.getString("info.newVersionAvailable.stopNagging");
        String later = myMainBundle.getString("info.newVersionAvailable.later");
        String download = myMainBundle.getString("info.newVersionAvailable.download");
        String cancel = myMainBundle.getString("gui.cancel");
        if (autoCheck) {
            pane.setOptions(new String[] {download, later, stopNagging});
        } else {
            pane.setOptions(new String[] {download, cancel});
        }
        pane.setInitialValue(download);
        JDialog dialog = pane.createDialog(myFrame, MessageFormat.format(myMainBundle.getString("info.newVersionAvailable.title"),
                                                                         updateInfo.getVersion()));
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
        if (pane.getValue() == stopNagging) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("noNagVersion", updateInfo.getVersion());
        }
        return pane.getValue() == download;
    }

    public static class ITunesLibraryFileFilter extends javax.swing.filechooser.FileFilter {
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