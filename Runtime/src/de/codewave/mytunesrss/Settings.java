package de.codewave.mytunesrss;

import de.codewave.embedtomcat.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

import sun.plugin.viewer.*;

/**
 * de.codewave.mytunesrss.Settings
 */
public class Settings {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";
    private static final String DEFAULT_LIBRARY_PATH = "";
    private static final String DEFAULT_AUTH_USER = "";
    private static final String DEFAULT_AUTH_PASS = "";
    private static final boolean DEFAULT_USE_AUTH = false;
    private static final String DEFAULT_TOMCAT_PORT = "8080";

    private final ResourceBundle myMainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");

    private JFrame myFrame;
    private JTextField myPort;
    private JTextField myTunesXmlPath;
    private JPanel myRootPanel;
    private JLabel myStatusText;
    private JButton myStartStopButton;
    private JButton myQuitButton;
    private JButton myLookupButton;
    private JCheckBox myUseAuthCheck;
    private JTextField myUsername;
    private JPasswordField myPassword;
    private JPanel myAuthInfoPanel;
    private Embedded myServer;

    public Settings(JFrame frame) {
        myFrame = frame;
        setStatus(myMainBundle.getString("info.server.idle"));
        myPort.setText(Preferences.userRoot().node("/de/codewave/mytunesrss").get("port", DEFAULT_TOMCAT_PORT));
        myTunesXmlPath.setText(Preferences.userRoot().node("/de/codewave/mytunesrss").get("library", DEFAULT_LIBRARY_PATH));
        myUsername.setText(Preferences.userRoot().node("/de/codewave/mytunesrss").get("authUsername", DEFAULT_AUTH_USER));
        myPassword.setText(Preferences.userRoot().node("/de/codewave/mytunesrss").get("authPassword", DEFAULT_AUTH_PASS));
        myUseAuthCheck.setSelected(Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("useAuth", DEFAULT_USE_AUTH));
        handleAuthInfoPanel();
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
        myUseAuthCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleAuthInfoPanel();
            }
        });

        myRootPanel.validate();
    }

    private void handleAuthInfoPanel() {
        myUsername.setEnabled(myUseAuthCheck.isSelected());
        myPassword.setEnabled(myUseAuthCheck.isSelected());
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
        } else {
            disableButtons();
            disableConfig();
            myRootPanel.validate();
            setStatus(myMainBundle.getString("info.server.starting"));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        myServer = EmbeddedTomcat.createServer("MyTunesRss", null, serverPort, new File("."), "ROOT", "");
                        System.setProperty("mytunesrss.iTunesLibrary", library.getCanonicalPath());
                        System.setProperty("mytunesrss.useAuth", Boolean.toString(myUseAuthCheck.isSelected()));
                        System.setProperty("mytunesrss.authUsername", myUsername.getText());
                        System.setProperty("mytunesrss.authPassword", new String(myPassword.getPassword()));
                        myServer.start();
                        myStartStopButton.setText(myMainBundle.getString("gui.settings.button.stopServer"));
                        myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.stopServer"));
                    } catch (LifecycleException e) {
                        if (e.getMessage().contains("BindException")) {
                            showErrorMessage(myMainBundle.getString("error.server.startFailureBindException"));
                        } else {
                            showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        }
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig();
                    } catch (IOException e) {
                        showErrorMessage(myMainBundle.getString("error.server.startFailure") + e.getMessage());
                        setStatus(myMainBundle.getString("info.server.idle"));
                        enableConfig();
                    }
                    enableButtons();
                    myRootPanel.validate();
                    setStatus(myMainBundle.getString("info.server.running"));
                }
            }).start();
        }
    }

    public void doStopServer() {
        disableButtons();
        myRootPanel.validate();
        new Thread(new Runnable() {
            public void run() {
                try {
                    setStatus(myMainBundle.getString("info.server.stopping"));
                    myServer.stop();
                    myServer = null;
                    setStatus(myMainBundle.getString("info.server.idle"));
                    enableConfig();
                    myStartStopButton.setText(myMainBundle.getString("gui.settings.button.startServer"));
                    myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.startServer"));
                } catch (LifecycleException e) {
                    showErrorMessage(myMainBundle.getString("error.server.stopFailure") + e.getMessage());
                    setStatus(myMainBundle.getString("info.server.running"));
                }
                enableButtons();
                myRootPanel.validate();
            }
        }).start();
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
            String savedPort = Preferences.userRoot().node("/de/codewave/mytunesrss").get("port", DEFAULT_TOMCAT_PORT);
            String savedPath = Preferences.userRoot().node("/de/codewave/mytunesrss").get("library", DEFAULT_LIBRARY_PATH);
            String savedUsername = Preferences.userRoot().node("/de/codewave/mytunesrss").get("authUsername", DEFAULT_AUTH_USER);
            String savedPassword = Preferences.userRoot().node("/de/codewave/mytunesrss").get("authPassword", DEFAULT_AUTH_PASS);
            boolean savedUseAuth = Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("useAuth", DEFAULT_USE_AUTH);
            if (!myPort.getText().trim().equals(savedPort) || !myTunesXmlPath.getText().trim().equals(savedPath) || !myUsername.getText().trim().equals(savedUsername) || !new String(myPassword.getPassword()).trim().equals(savedPassword) || myUseAuthCheck.isSelected() != savedUseAuth) {
                if (JOptionPane.showOptionDialog(myRootPanel.getTopLevelAncestor(),
                                                 myMainBundle.getString("dialog.saveSettingsQuestion.message"),
                                                 myMainBundle.getString("dialog.saveSettingsQuestion.title"),
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 null,
                                                 null) == JOptionPane.YES_OPTION) {
                    Preferences.userRoot().node("/de/codewave/mytunesrss").put("port", myPort.getText().trim());
                    Preferences.userRoot().node("/de/codewave/mytunesrss").put("library", myTunesXmlPath.getText().trim());
                    Preferences.userRoot().node("/de/codewave/mytunesrss").put("authUsername", myUsername.getText().trim());
                    Preferences.userRoot().node("/de/codewave/mytunesrss").put("authPassword", new String(myPassword.getPassword()).trim());
                    Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("useAuth", myUseAuthCheck.isSelected());
                }
            }
            System.exit(0);
        } else {
            showErrorMessage(myMainBundle.getString("error.quitWhileStartingOrStopping"));
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(getRootPanel().getTopLevelAncestor(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons() {
        myStartStopButton.setEnabled(true);
        myQuitButton.setEnabled(true);
    }

    private void disableButtons() {
        myStartStopButton.setEnabled(false);
        myQuitButton.setEnabled(false);
    }

    private void enableConfig() {
        myLookupButton.setEnabled(true);
        myPort.setEnabled(true);
        myTunesXmlPath.setEnabled(true);
    }

    private void disableConfig() {
        myLookupButton.setEnabled(false);
        myPort.setEnabled(false);
        myTunesXmlPath.setEnabled(false);
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