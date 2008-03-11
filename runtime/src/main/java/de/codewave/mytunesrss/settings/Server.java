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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Server settings panel
 */
public class Server implements MyTunesRssEventListener {
    private static final Log LOG = LogFactory.getLog(Server.class);

    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JLabel myServerStatusLabel;
    private JButton myServerInfoButton;
    private JCheckBox myAutoStartServerInput;
    private JTextField myServerNameInput;
    private JCheckBox myAvailableOnLocalNetInput;
    private JCheckBox myTempZipArchivesInput;
    private JLabel myServerNameLabel;

    public void init() {
        initRegistration();
        initValues();
        MyTunesRssEventManager.getInstance().addListener(this);
        myAutoStartServerInput.addActionListener(new AutoStartServerInputListener());
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
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
                        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.running"), null);
                        myRootPanel.validate();
                        setGuiMode(GuiMode.ServerRunning);
                        break;
                    case SERVER_STOPPED:
                        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
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
        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
        myTempZipArchivesInput.setSelected(MyTunesRss.CONFIG.isLocalTempArchive());
    }

    private void initRegistration() {
        myAvailableOnLocalNetInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myServerNameLabel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myServerNameInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
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

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        SwingUtils.enableElementAndLabel(myPortInput, !serverActive);
        myAutoStartServerInput.setEnabled(!serverActive);
        myTempZipArchivesInput.setEnabled(!serverActive);
        SwingUtils.enableElementAndLabel(myServerNameInput, !serverActive && myAvailableOnLocalNetInput.isSelected());
    }

    public void setServerStatus(String text, String tooltipText) {
        if (text != null) {
            myServerStatusLabel.setText(text);
        }
        if (tooltipText != null) {
            myServerStatusLabel.setToolTipText(tooltipText);
        }
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

    public class ServerInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new ServerInfo().display(MyTunesRss.ROOT_FRAME, myPortInput.getText());
        }
    }
}