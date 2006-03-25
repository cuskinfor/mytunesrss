package de.codewave.mytunesrss;

import de.codewave.embedtomcat.*;
import de.codewave.utils.swing.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.Settings
 */
public class Settings {
    private static final Log LOG = LogFactory.getLog(Settings.class);

    private JTextField myPort;
    private JTextField myTunesXmlPath;
    private JPanel myRootPanel;
    private JPanel myIdlePanel;
    private JLabel myStatusText;
    private Embedded myServer;

    public Settings() {
        SwingUtils.assignActionListeners(myRootPanel, this);
        myStatusText.setText("Please configure your server.");
        myPort.setText(Preferences.userRoot().get("/de/codewave/mytunesrss/port", "8080"));
        myTunesXmlPath.setText(Preferences.userRoot().get("/de/codewave/mytunesrss/library", ""));
        myRootPanel.validate();
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    @ActionListenerComponent("lookupButton")
    public void doLookupLibraryFile(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ITunesLibraryFileFilter());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new ITunesLibraryFileFilter());
        fileChooser.setDialogTitle("Select your iTunes Library");
        if (fileChooser.showDialog(myRootPanel, null) == JFileChooser.APPROVE_OPTION) {
            try {
                myTunesXmlPath.setText(fileChooser.getSelectedFile().getCanonicalPath());
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not approve file selection.", e);
                }
            }
        }
    }

    @ActionListenerComponent("startStopButton")
    public void doStartStopServer(ActionEvent event) {
        if (myServer == null) {
            doStartServer(event);
        } else {
            doStopServer(event);
        }
    }

    public void doStartServer(ActionEvent event) {
        int port;
        try {
            port = Integer.parseInt(myPort.getText());
        } catch (NumberFormatException e) {
            port = -1;
        }
        final int serverPort = port;
        final File library = new File(myTunesXmlPath.getText());
        if (port < 1 || port > 65535) {
            myStatusText.setText("Please enter a port number between 1 and 65535.");
        } else if (!new ITunesLibraryFileFilter().accept(library)) {
            myStatusText.setText("Please select the \"iTunes Music Library.xml\" file.");
        } else {
            myIdlePanel.setEnabled(false);
            myRootPanel.validate();
            myStatusText.setText("Starting server... please wait.");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        myServer = EmbeddedTomcat.createServer("MyTunesRss", null, serverPort, new File("webapps"), "ROOT", "");
                        System.setProperty("iTunesLibrary", library.getCanonicalPath());
                        myServer.start();
                    } catch (Exception e) {
                        myStatusText.setText("Could not start server: " + e.getMessage());
                    }
                    myRootPanel.validate();
                    myStatusText.setText("Server running.");
                }
            }).start();
        }
    }

    @ActionListenerComponent("stopButton")
    public void doStopServer(ActionEvent event) {
        myIdlePanel.setEnabled(false);
        myRootPanel.validate();
        new Thread(new Runnable() {
            public void run() {
                try {
                    myStatusText.setText("Stopping server... please wait.");
                    myServer.stop();
                    myServer = null;
                    myStatusText.setText("Please configure your server.");
                } catch (LifecycleException e) {
                    myStatusText.setText("Could not stop server: " + e.getMessage());
                }
                myIdlePanel.setEnabled(true);
                myRootPanel.validate();
            }
        }).start();
    }

    @ActionListenerComponent("quitButton")
    public void doQuitApplication(ActionEvent event) {
        if (myServer != null) {
            doStopServer(event);
        }
        Preferences.userRoot().put("/de/codewave/mytunesrss/port", myPort.getText());
        Preferences.userRoot().put("/de/codewave/mytunesrss/library", myTunesXmlPath.getText());
        System.exit(0);
    }

    public static class ITunesLibraryFileFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            return f != null && f.exists() && f.getName().toLowerCase().equals("itunes music library.xml");
        }

        public String getDescription() {
            return "iTunes Library";
        }
    }
}