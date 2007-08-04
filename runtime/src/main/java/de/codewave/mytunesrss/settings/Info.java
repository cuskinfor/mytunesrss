/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private static final Log LOG = LogFactory.getLog(Info.class);

    private JPanel myRootPanel;
    private JLabel myRegistrationNameInput;
    private JLabel myExpirationInput;
    private JTextArea myUnregisteredTextArea;
    private JLabel myRegistrationNameLabel;
    private JLabel myExpirationLabel;
    private JButton myRegisterButton;
    private JCheckBox myLogDebugInput;
    private JButton mySupportContactButton;

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
        mySupportContactButton.addActionListener(new SupportContactActionListener());
        myRegisterButton.addActionListener(new LicenseLookupButtonListener());
        myLogDebugInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Level level = Level.INFO;
                if (myLogDebugInput.isSelected()) {
                    level = Level.DEBUG;
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Setting codewave log to DEBUG logging.");
                    }
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Setting codewave log to INFO logging.");
                    }
                }
                LoggerRepository repository = Logger.getRootLogger().getLoggerRepository();
                for (Enumeration loggerEnum = repository.getCurrentLoggers(); loggerEnum.hasMoreElements(); ) {
                    Logger logger = (Logger)loggerEnum.nextElement();
                    if (logger.getName().startsWith("de.codewave.")) {
                        logger.setLevel(level);
                    }
                }
                Logger.getLogger("de.codewave").setLevel(level);
            }
        });
    }

    private void refreshRegistration() {
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            myRegistrationNameLabel.setVisible(true);
            myRegistrationNameInput.setVisible(true);
            myRegistrationNameInput.setText(MyTunesRss.REGISTRATION.getName());
            myExpirationLabel.setVisible(MyTunesRss.REGISTRATION.isExpirationDate());
            myExpirationInput.setVisible(MyTunesRss.REGISTRATION.isExpirationDate());
            myExpirationInput.setText(MyTunesRss.REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString("common.dateFormat")));
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
            FileDialog fileDialog = new FileDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("dialog.loadLicense"), FileDialog.LOAD);
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                MyTunesRssRegistration.register(new File(fileDialog.getDirectory(), fileDialog.getFile()));
            }
        }
    }

    public class SupportContactActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new SupportContact().display(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.supportRequest"), MyTunesRss.BUNDLE.getString(
                    "settings.supportInfo"));
        }
    }
}