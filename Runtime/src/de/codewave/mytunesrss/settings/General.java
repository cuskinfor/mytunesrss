/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.network.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

/**
 * General settings panel
 */
public class General {
    private static final Log LOG = LogFactory.getLog(General.class);
    private static final String LIBRARY_XML_FILE_NAME = "iTunes Music Library.xml";

    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JPasswordField myPasswordInput;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
    private Settings mySettingsForm;
    private JLabel myServerStatusLabel;
    private JButton myServerInfoButton;

    public JPasswordField getPasswordInput() {
        return myPasswordInput;
    }

    public JTextField getPortInput() {
        return myPortInput;
    }

    public JTextField getTunesXmlPathInput() {
        return myTunesXmlPathInput;
    }

    public void init(Settings settingsForm) {
        mySettingsForm = settingsForm;
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myPasswordInput.setText(MyTunesRss.CONFIG.getPassword());
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
    }

    public void setServerRunningStatus(int serverPort) {
        String[] localAddresses = NetworkUtils.getLocalNetworkAddresses();
        if (localAddresses.length == 0) {
            setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.running"), null);
        } else {
            setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.running") + " [ http://" + localAddresses[0] + ":" + serverPort + " ] ", null);
        }
        myRootPanel.validate();
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
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myPortInput, true);
                SwingUtils.enableElementAndLabel(myPasswordInput, true);
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
            FileDialog fileDialog = new FileDialog(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("dialog.loadITunes"), FileDialog.LOAD);
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                File sourceFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                try {
                    myTunesXmlPathInput.setText(sourceFile.getCanonicalPath());
                } catch (IOException e) {
                    SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.lookupLibraryXml") + e.getMessage());
                }
            }
        }
    }

    public static class ITunesLibraryFileFilter implements FilenameFilter {
        public boolean accept(File directory, String filename) {
            return filename != null && (new File(directory, filename).isFile() && LIBRARY_XML_FILE_NAME.equalsIgnoreCase(filename));
        }
    }

    public class ServerInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new ServerInfo().display(mySettingsForm.getFrame(), myPortInput.getText());
        }
    }
}