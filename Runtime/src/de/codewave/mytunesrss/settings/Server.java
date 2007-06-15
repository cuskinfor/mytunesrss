/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.network.*;
import de.codewave.utils.swing.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Server settings panel
 */
public class Server {
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
        myAutoStartServerInput.addActionListener(new AutoStartServerInputListener());
        myAutoStartServerInput.setSelected(MyTunesRss.CONFIG.isAutoStartServer());
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myServerNameInput.setText(MyTunesRss.CONFIG.getServerName());
        myAvailableOnLocalNetInput.setSelected(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected());
        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
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
        myTempZipArchivesInput.setSelected(MyTunesRss.CONFIG.isLocalTempArchive());
        if (myAutoStartServerInput.isSelected()) {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.EnableAutoStartServer);
        } else {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DisableAutoStartServer);
        }
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myPortInput, 1, 65535, false, MyTunesRssUtils.getBundleString(
                "error.illegalServerPort")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myServerNameInput, MyTunesRssUtils.getBundleString("error.emptyServerName")));
    }

    private void initRegistration() {
        myAvailableOnLocalNetInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myServerNameLabel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
        myServerNameInput.setVisible(MyTunesRss.REGISTRATION.isRegistered());
    }

    public Dimension getContentDimension() {
        Insets insets = ((AbstractLayout)myRootPanel.getLayout()).getMargin();
        return new Dimension(myRootPanel.getWidth() - insets.left - insets.right, myRootPanel.getHeight() - insets.top - insets.bottom);
    }

    public void setServerRunningStatus(int serverPort) {
        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.running"), null);
        myRootPanel.validate();
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
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myPortInput, false);
                myAutoStartServerInput.setEnabled(false);
                myTempZipArchivesInput.setEnabled(false);
                SwingUtils.enableElementAndLabel(myServerNameInput, false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myPortInput, true);
                myAutoStartServerInput.setEnabled(true);
                myTempZipArchivesInput.setEnabled(true);
                SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected());
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

    public class AutoStartServerInputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (myAutoStartServerInput.isSelected()) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.EnableAutoStartServer);
            } else {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DisableAutoStartServer);
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