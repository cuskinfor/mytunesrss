/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private static final Log LOG = LogFactory.getLog(Info.class);

    private JPanel myRootPanel;
    private JTextField myRegistrationNameInput;
    private JTextField myExpirationInput;
    private JTextArea myUnregisteredTextArea;
    private JLabel myRegistrationNameLabel;
    private JLabel myExpirationLabel;
    private JButton myRegisterButton;

    private void createUIComponents() {
        myUnregisteredTextArea = new JTextArea() {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, 0);
            }
        };
    }

    public void init() {
        refreshRegistration();
        myRegisterButton.addActionListener(new LicenseLookupButtonListener());
    }

    private void refreshRegistration() {
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            myRegistrationNameLabel.setVisible(true);
            myRegistrationNameInput.setVisible(true);
            myRegistrationNameInput.setText(MyTunesRss.REGISTRATION.getName());
            myExpirationLabel.setVisible(MyTunesRss.REGISTRATION.isExpirationDate());
            myExpirationInput.setVisible(MyTunesRss.REGISTRATION.isExpirationDate());
            myExpirationInput.setText(MyTunesRss.REGISTRATION.getExpiration(MyTunesRss.BUNDLE.getString("common.dateFormat")));
            myUnregisteredTextArea.setVisible(false);
        } else {
            myRegistrationNameLabel.setVisible(false);
            myRegistrationNameInput.setVisible(false);
            myExpirationLabel.setVisible(false);
            myExpirationInput.setVisible(false);
            myUnregisteredTextArea.setVisible(true);
        }
    }

    public class LicenseLookupButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            FileDialog fileDialog = new FileDialog(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.loadLicense"), FileDialog.LOAD);
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                File registrationFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                try {
                    if (MyTunesRssRegistration.isValidRegistration(registrationFile)) {
                        MyTunesRssRegistration registration = new MyTunesRssRegistration();
                        registration.init(registrationFile, false);
                        if (registration.isExpired()) {
                            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.loadLicenseExpired", registration.getExpiration(
                                    MyTunesRss.BUNDLE.getString("common.dateFormat"))));
                        } else if (!registration.isRegistered()) {
                            MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.loadLicense"));
                        } else {
                            copyFile(registrationFile, new File(
                                    PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/MyTunesRSS.key"));
                            MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("error.loadLicenseOk",
                                                                                                                   registration.getName()));
                        }
                    } else {
                        MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.loadLicense"));
                    }
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.loadLicense"));
                }
            }
        }
    }

    private void copyFile(File source, File destination) {
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(source));
            outputStream = new BufferedOutputStream(new FileOutputStream(destination));
            for (int v = inputStream.read(); v != -1; v = inputStream.read()) {
                outputStream.write(v);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not copy file.", e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // intentionally left blank
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // intentionally left blank
                }
            }
        }
    }
}