/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.network.*;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * General settings panel
 */
public class General {
    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JPasswordField myPasswordInput;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
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

    public void init() {
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
        myPasswordInput.addFocusListener(new PasswordInputListener());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        User defaultUser = MyTunesRss.CONFIG.getUser("default");
        if (defaultUser != null && defaultUser.getPasswordHash() != null && defaultUser.getPasswordHash().length > 0) {
            setPasswordVisible();
        } else {
            setPasswordHidden();
        }
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
    }

    private void setPasswordVisible() {
        myPasswordInput.setText("");
        myPasswordInput.setEchoChar((char)0);
        Font font = myPasswordInput.getFont();
        myPasswordInput.setFont(new Font(font.getName(), font.getStyle() | Font.ITALIC, font.getSize()));
        myPasswordInput.setForeground(Color.LIGHT_GRAY);
        myPasswordInput.setText(MyTunesRss.BUNDLE.getString("settings.passwordHasBeenSet"));
    }

    private void setPasswordHidden() {
        myPasswordInput.setText("");
        myPasswordInput.setEchoChar('*');
        Font font = myPasswordInput.getFont();
        myPasswordInput.setFont(new Font(font.getName(), font.getStyle() & (Integer.MAX_VALUE - Font.ITALIC), font.getSize()));
        myPasswordInput.setForeground(Color.BLACK);
    }

    public void setServerRunningStatus(int serverPort) {
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.running"), null);
        myRootPanel.validate();
    }

    public void updateConfigFromGui() {
        try {
            MyTunesRss.CONFIG.setPort(Integer.parseInt(myPortInput.getText().trim()));
        } catch (NumberFormatException e) {
            MyTunesRss.CONFIG.setPort(-1);
        }
        MyTunesRss.CONFIG.setLibraryXml(myTunesXmlPathInput.getText().trim());
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
            FileDialog fileDialog = new FileDialog(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.loadITunes"), FileDialog.LOAD);
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                File sourceFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                try {
                    myTunesXmlPathInput.setText(sourceFile.getCanonicalPath());
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.lookupLibraryXml") + e.getMessage());
                }
            }
        }
    }

    public class ServerInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new ServerInfo().display(MyTunesRss.ROOT_FRAME, myPortInput.getText());
        }
    }

    public class PasswordInputListener implements FocusListener {
        private boolean myPreviousPasswordSet;

        public void focusGained(FocusEvent focusEvent) {
            myPreviousPasswordSet = myPasswordInput.getPassword().length > 0;
            setPasswordHidden();
        }

        public void focusLost(FocusEvent focusEvent) {
            String password = new String(myPasswordInput.getPassword()).trim();
            if (StringUtils.isNotEmpty(password)) {
                User user = MyTunesRss.CONFIG.getUser("default");
                if (user == null) {
                    user = new User("default");
                    MyTunesRss.CONFIG.addUser(user);
                }
                user.setPassword(password);
                setPasswordVisible();
            } else if (myPreviousPasswordSet) {
                setPasswordVisible();
            }
        }
    }
}