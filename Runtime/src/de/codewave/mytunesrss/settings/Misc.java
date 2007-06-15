/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import com.intellij.uiDesigner.core.*;

/**
 * de.codewave.mytunesrss.settings.Misc
 */
public class Misc implements MyTunesRssEventListener {
    private JButton mySupportContactButton;
    private JPanel myRootPanel;
    private JTextField myUsernameInput;
    private PasswordHashField myPasswordInput;
    private JTextField myProxyHostInput;
    private JTextField myProxyPortInput;
    private JCheckBox myUseProxyInput;
    private JLabel myPasswordLabel;
    private JCheckBox myQuitConfirmationInput;
    private JCheckBox myUpdateOnStartInput;
    private JButton myProgramUpdateButton;
    private JPanel myMyTunesRssComPanel;
    private boolean myUpdateOnStartInputCache;
    private boolean myAutoStartServer;

    public Misc() {
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void init() {
        initRegistration();
        mySupportContactButton.addActionListener(new SupportContactActionListener());
        myUsernameInput.setText(MyTunesRss.CONFIG.getMyTunesRssComUser());
        myPasswordInput.setPasswordHash(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash());
        myUseProxyInput.setSelected(MyTunesRss.CONFIG.isProxyServer());
        myUseProxyInput.addActionListener(new UseProxyActionListener());
        SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
        SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
        myProxyHostInput.setText(MyTunesRss.CONFIG.getProxyHost());
        int port = MyTunesRss.CONFIG.getProxyPort();
        if (port > 0 && port < 65536) {
            myProxyPortInput.setText(Integer.toString(port));
        } else {
            myProxyPortInput.setText("");
        }
        myQuitConfirmationInput.setSelected(MyTunesRss.CONFIG.isQuitConfirmation());
        myProgramUpdateButton.addActionListener(new ProgramUpdateButtonListener());
        myUpdateOnStartInput.setSelected(MyTunesRss.CONFIG.isCheckUpdateOnStart());
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myProxyHostInput, MyTunesRssUtils.getBundleString("error.emptyProxyHost")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myProxyPortInput, 1, 65535, false, MyTunesRssUtils.getBundleString(
                "error.illegalProxyPort")));
    }

    private void initRegistration() {
        myMyTunesRssComPanel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRssUtils.getBundleString("passwordHasBeenSet"), MyTunesRss.MESSAGE_DIGEST);
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myProxyHostInput, false);
                SwingUtils.enableElementAndLabel(myProxyPortInput, false);
                myUseProxyInput.setEnabled(false);
                SwingUtils.enableElementAndLabel(myUsernameInput, false);
                myPasswordLabel.setEnabled(false);
                myPasswordInput.setEnabled(false);
                myQuitConfirmationInput.setEnabled(false);
                myUpdateOnStartInput.setEnabled(false);
                myProgramUpdateButton.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
                SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
                myUseProxyInput.setEnabled(true);
                SwingUtils.enableElementAndLabel(myUsernameInput, true);
                myPasswordLabel.setEnabled(true);
                myPasswordInput.setEnabled(true);
                myQuitConfirmationInput.setEnabled(true);
                myUpdateOnStartInput.setEnabled(!myAutoStartServer);
                myProgramUpdateButton.setEnabled(true);
                break;
        }
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setMyTunesRssComUser(myUsernameInput.getText());
            if (myPasswordInput.getPasswordHash() != null) {
                MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(myPasswordInput.getPasswordHash());
            }
            MyTunesRss.CONFIG.setProxyServer(myUseProxyInput.isSelected());
            MyTunesRss.CONFIG.setProxyHost(myProxyHostInput.getText());
            MyTunesRss.CONFIG.setProxyPort(MyTunesRssUtils.getTextFieldInteger(myProxyPortInput, -1));
            MyTunesRss.CONFIG.setQuitConfirmation(myQuitConfirmationInput.isSelected());
            MyTunesRss.CONFIG.setCheckUpdateOnStart(myUpdateOnStartInput.isSelected());
        }
        return null;
    }

    public void handleEvent(MyTunesRssEvent event) {
        switch (event) {
            case EnableAutoStartServer:
                myUpdateOnStartInputCache = myUpdateOnStartInput.isSelected();
                myUpdateOnStartInput.setSelected(false);
                myUpdateOnStartInput.setEnabled(false);
                myAutoStartServer = true;
                break;
            case DisableAutoStartServer:
                myUpdateOnStartInput.setSelected(myUpdateOnStartInputCache);
                myUpdateOnStartInput.setEnabled(true);
                myAutoStartServer = false;
                break;
        }
    }

    public class SupportContactActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String messages = updateConfigFromGui();
            if (messages == null) {
                new SupportContact().display(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("dialog.supportRequest"), MyTunesRssUtils.getBundleString(
                        "settings.supportInfo"));
            } else {
                MyTunesRssUtils.showErrorMessage(messages);
            }
        }
    }

    public class UseProxyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
            SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
        }
    }

    public class ProgramUpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            UpdateUtils.checkForUpdate(false);
        }
    }
}
