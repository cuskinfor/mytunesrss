/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.MinMaxValueTextFieldValidation;
import de.codewave.utils.swing.NotEmptyTextFieldValidation;
import de.codewave.utils.swing.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Server settings panel
 */
public class Server implements MyTunesRssEventListener, SettingsForm {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JCheckBox myAutoStartServerInput;
    private JTextField myServerNameInput;
    private JCheckBox myAvailableOnLocalNetInput;
    private JCheckBox myTempZipArchivesInput;
    private JLabel myServerNameLabel;

    public void init() {
        initValues();
        MyTunesRssEventManager.getInstance().addListener(this);
        myAutoStartServerInput.addActionListener(new AutoStartServerInputListener());
        myAvailableOnLocalNetInput.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (MyTunesRss.WEBSERVER.isRunning()) {
                    if (myAvailableOnLocalNetInput.isSelected()) {
                        MulticastService.startListener();
                    } else {
                        MulticastService.stopListener();
                    }
                }
                SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected() && !MyTunesRss.WEBSERVER.isRunning());
            }
        });
        if (myAutoStartServerInput.isSelected()) {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.ENABLE_AUTO_START_SERVER);
        } else {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DISABLE_AUTO_START_SERVER);
        }
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myPortInput, 1, 65535, false, MyTunesRssUtils.getBundleString(
                "error.illegalServerPort")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myServerNameInput, MyTunesRssUtils.getBundleString("error.emptyServerName")));
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                    case DATABASE_UPDATE_STATE_CHANGED:
                        setGuiMode(GuiMode.DatabaseUpdating);
                        break;
                    case DATABASE_UPDATE_FINISHED:
                    case DATABASE_UPDATE_FINISHED_NOT_RUN:
                        setGuiMode(GuiMode.DatabaseIdle);
                        break;
                    case SERVER_STARTED:
                        setGuiMode(GuiMode.ServerRunning);
                        break;
                    case SERVER_STOPPED:
                        setGuiMode(GuiMode.ServerIdle);
                        break;
                }
            }
        });
    }

    private void initValues() {
        myAutoStartServerInput.setSelected(MyTunesRss.CONFIG.isAutoStartServer());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myServerNameInput.setText(MyTunesRss.CONFIG.getServerName());
        myAvailableOnLocalNetInput.setSelected(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected());
        myTempZipArchivesInput.setSelected(MyTunesRss.CONFIG.isLocalTempArchive());
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setPort(MyTunesRssUtils.getTextFieldInteger(myPortInput, -1));
            MyTunesRss.CONFIG.setAutoStartServer(myAutoStartServerInput.isSelected());
            MyTunesRss.CONFIG.setServerName(myServerNameInput.getText());
            MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNetInput.isSelected());
            MyTunesRss.CONFIG.setLocalTempArchive(myTempZipArchivesInput.isSelected());
        }
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        SwingUtils.enableElementAndLabel(myPortInput, !serverActive);
        myAutoStartServerInput.setEnabled(!serverActive);
        myTempZipArchivesInput.setEnabled(!serverActive);
        SwingUtils.enableElementAndLabel(myServerNameInput, !serverActive && myAvailableOnLocalNetInput.isSelected());
    }

    // todo: get name from i18n properties
    public String toString() {
        return "Server settings";
    }

    public class AutoStartServerInputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (myAutoStartServerInput.isSelected()) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.ENABLE_AUTO_START_SERVER);
            } else {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DISABLE_AUTO_START_SERVER);
            }
            myRootPanel.validate();
        }
    }
}