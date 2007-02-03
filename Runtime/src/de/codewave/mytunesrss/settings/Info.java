/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.components.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private JButton mySupportContactButton;
    private JPanel myRootPanel;
    private JTextField myUsernameInput;
    private PasswordHashField myPasswordInput;
    private JTextField myProxyHostInput;
    private JTextField myProxyPortInput;
    private JCheckBox myUseProxyInput;
    private JLabel myPasswordLabel;

    public void init() {
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
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myProxyHostInput, MyTunesRss.BUNDLE.getString("error.emptyProxyHost")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myProxyPortInput, 1, 65535, false, MyTunesRss.BUNDLE.getString(
                "error.illegalProxyPort")));
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRss.BUNDLE.getString("passwordHasBeenSet"), MyTunesRss.MESSAGE_DIGEST);
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
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
                SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
                myUseProxyInput.setEnabled(true);
                SwingUtils.enableElementAndLabel(myUsernameInput, true);
                myPasswordLabel.setEnabled(true);
                myPasswordInput.setEnabled(true);
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
        }
        return null;
    }

    public class SupportContactActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String messages = updateConfigFromGui();
            if (messages == null) {
                new SupportContact().display(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.supportRequest"), MyTunesRss.BUNDLE.getString(
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
}
