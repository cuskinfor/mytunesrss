package de.codewave.mytunesrss;

import de.codewave.embedtomcat.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.Settings
 */
public class Settings {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";

    private final ResourceBundle myMainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    private JTextField myPort;
    private JTextField myTunesXmlPath;
    private JPanel myRootPanel;
    private JLabel myStatusText;
    private JButton myStartStopButton;
    private JButton myQuitButton;
    private JButton myLookupButton;
    private Embedded myServer;

    public Settings() {
        setStatus(myMainBundle.getString("info.server.idle"));
        myPort.setText(Preferences.userRoot().get("/de/codewave/mytunesrss/port", "8080"));
        myTunesXmlPath.setText(Preferences.userRoot().get("/de/codewave/mytunesrss/library", ""));
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
        myRootPanel.validate();
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void doLookupLibraryFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ITunesLibraryFileFilter());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new ITunesLibraryFileFilter());
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
            port = Integer.parseInt(myPort.getText());
        } catch (NumberFormatException e) {
            port = MIN_PORT - 1;
        }
        final int serverPort = port;
        final File library = new File(myTunesXmlPath.getText());
        if (port < MIN_PORT || port > MAX_PORT) {
            showErrorMessage(myMainBundle.getString("error.startServer.port"));
        } else if (!new ITunesLibraryFileFilter().accept(library)) {
            showErrorMessage(myMainBundle.getString("error.startServer.libraryXmlFile"));
        } else {
            disableButtons();
            disableConfig();
            myRootPanel.validate();
            setStatus(myMainBundle.getString("info.server.starting"));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        myServer = EmbeddedTomcat.createServer("MyTunesRss", null, serverPort, new File("webapps"), "ROOT", "");
                        System.setProperty("iTunesLibrary", library.getCanonicalPath());
                        myServer.start();
                        myStartStopButton.setText(myMainBundle.getString("gui.settings.button.stopServer"));
                        myStartStopButton.setToolTipText(myMainBundle.getString("gui.settings.tooltip.stopServer"));
                    } catch (Exception e) {
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
            Preferences.userRoot().put("/de/codewave/mytunesrss/port", myPort.getText());
            Preferences.userRoot().put("/de/codewave/mytunesrss/library", myTunesXmlPath.getText());
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
        public boolean accept(File f) {
            return f != null && f.exists() && LIBRARY_XML_FILE_NAME.equalsIgnoreCase(f.getName());
        }

        public String getDescription() {
            return "iTunes Library";
        }
    }
}