/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.components.*;
import org.apache.commons.lang.*;

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

    public void init() {
        mySupportContactButton.addActionListener(new SupportContactActionListener());
        myUsernameInput.setText(MyTunesRss.CONFIG.getMyTunesRssComUser());
        myPasswordInput.setPasswordHash(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash());
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRss. BUNDLE.getString("passwordHasBeenSet"), MyTunesRss.MESSAGE_DIGEST);
    }

    public void updateConfigFromGui() {
        MyTunesRss.CONFIG.setMyTunesRssComUser(myUsernameInput.getText());
        if (myPasswordInput.getPasswordHash() != null) {
            MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(myPasswordInput.getPasswordHash());
        }
    }

    public static class SupportContactActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new SupportContact().display(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.supportRequest"), MyTunesRss.BUNDLE.getString(
                    "settings.supportInfo"));
        }
    }
}
