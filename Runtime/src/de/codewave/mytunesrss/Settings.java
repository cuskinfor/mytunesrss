package de.codewave.mytunesrss;

import de.codewave.embedtomcat.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.*;
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
    private JLabel myStatusText;
    private JButton myStartStopButton;
    private JButton myQuitButton;
    private JButton myLookupButton;
    private Embedded myServer;

    public Settings() {
        setStatus("Please configure your server.", false);
        myPort.setText(Preferences.userRoot().get("/de/codewave/mytunesrss/port", "8080"));
        myTunesXmlPath.setText(Preferences.userRoot().get("/de/codewave/mytunesrss/library", ""));
        myStartStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartStopServer(e);
            }
        });
        myQuitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuitApplication(e);
            }
        });
        myLookupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLookupLibraryFile(e);
            }
        });
        myRootPanel.validate();
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

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
            setStatus("Please enter a port number between 1 and 65535.", true);
        } else if (!new ITunesLibraryFileFilter().accept(library)) {
            setStatus("Please select the \"iTunes Music Library.xml\" file.", true);
        } else {
            disableButtons();
            disableConfig();
            myRootPanel.validate();
            setStatus("Starting server... please wait.", false);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        myServer = EmbeddedTomcat.createServer("MyTunesRss", null, serverPort, new File("webapps"), "ROOT", "");
                        System.setProperty("iTunesLibrary", library.getCanonicalPath());
                        myServer.start();
                        myStartStopButton.setText("Stop myTunesRSS Webserver");
                    } catch (Exception e) {
                        setStatus("Could not start server: " + e.getMessage(), true);
                        enableConfig();
                    }
                    enableButtons();
                    myRootPanel.validate();
                    setStatus("Server running.", false);
                }
            }).start();
        }
    }

    public void doStopServer(ActionEvent event) {
        disableButtons();
        myRootPanel.validate();
        new Thread(new Runnable() {
            public void run() {
                try {
                    setStatus("Stopping server... please wait.", false);
                    myServer.stop();
                    myServer = null;
                    setStatus("Please configure your server.", false);
                    enableConfig();
                    myStartStopButton.setText("Start myTunesRSS Webserver");
                } catch (LifecycleException e) {
                    setStatus("Could not stop server: " + e.getMessage(), true);
                }
                enableButtons();
                myRootPanel.validate();
            }
        }).start();
    }

    private void setStatus(String text, boolean error) {
        myStatusText.setBackground(error ? Color.RED : Color.WHITE);
        myStatusText.setForeground(error ? Color.WHITE : Color.BLACK);
        myStatusText.setText(text);
    }

    public void doQuitApplication(ActionEvent event) {
        if (myServer != null) {
            doStopServer(event);
        }
        Preferences.userRoot().put("/de/codewave/mytunesrss/port", myPort.getText());
        Preferences.userRoot().put("/de/codewave/mytunesrss/library", myTunesXmlPath.getText());
        System.exit(0);
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
            return f != null && f.exists() && f.getName().toLowerCase().equals("itunes music library.xml");
        }

        public String getDescription() {
            return "iTunes Library";
        }
    }
}