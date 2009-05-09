/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info implements MyTunesRssEventListener, SettingsForm {
    private static final Logger LOG = LoggerFactory.getLogger(Info.class);

    private JPanel myRootPanel;
    private JLabel myRegistrationNameInput;
    private JLabel myExpirationInput;
    private JLabel myRegistrationNameLabel;
    private JLabel myExpirationLabel;
    private JButton myRegisterButton;
    private JComboBox myLogLevelInput;
    private JButton mySupportContactButton;

    public Info() {
        MyTunesRssEventManager.getInstance().addListener(this);
        refreshRegistration();
        mySupportContactButton.addActionListener(new SupportContactActionListener());
        myRegisterButton.addActionListener(new LicenseLookupButtonListener());
        myLogLevelInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MyTunesRssUtils.setCodewaveLogLevel(Level.toLevel(myLogLevelInput.getSelectedItem().toString()));
            }
        });
        myLogLevelInput.addItem("OFF");
        myLogLevelInput.addItem("ERROR");
        myLogLevelInput.addItem("WARN");
        myLogLevelInput.addItem("INFO");
        myLogLevelInput.addItem("DEBUG");
    }

    public void forceRegistration() {
        myRegisterButton.doClick();
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                }
            }
        });
    }

    public void initValues() {
        myLogLevelInput.setSelectedItem(MyTunesRss.CONFIG.getCodewaveLogLevel().toString());
    }

    private void refreshRegistration() {
        myRegistrationNameLabel.setVisible(true);
        myRegistrationNameInput.setVisible(true);
        myRegistrationNameInput.setText(MyTunesRss.REGISTRATION.getName());
        myExpirationLabel.setVisible(MyTunesRss.REGISTRATION.isExpirationDate());
        myExpirationInput.setVisible(MyTunesRss.REGISTRATION.isExpirationDate());
        myExpirationInput.setText(MyTunesRss.REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString("common.dateFormat")));
    }

    public String updateConfigFromGui() {
        MyTunesRss.CONFIG.setCodewaveLogLevel(Level.toLevel(myLogLevelInput.getSelectedItem().toString()));
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.info.title");
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