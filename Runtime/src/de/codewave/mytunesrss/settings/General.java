/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;

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
    private JTextField myBaseDirInput;
    private JButton myBaseDirLookupButton;
    private JSpinner myArtistLevelInput;
    private JSpinner myAlbumLevelInput;

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
        myPasswordInput.setText(
                defaultUser != null && defaultUser.getPasswordHash() != null && defaultUser.getPasswordHash().length > 0 ? "dummypassword" : "");
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myBaseDirInput.setText(MyTunesRss.CONFIG.getBaseDir());
        SpinnerNumberModel artistModel = new SpinnerNumberModel(MyTunesRss.CONFIG.getFileSystemArtistNameFolder(), 0, 5, 1);
        SpinnerNumberModel albumModel = new SpinnerNumberModel(MyTunesRss.CONFIG.getFileSystemAlbumNameFolder(), 0, 5, 1);
        myArtistLevelInput.setModel(artistModel);
        myAlbumLevelInput.setModel(albumModel);
        myBaseDirLookupButton.addActionListener(new BaseDirLookupButtonListener());
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
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
        MyTunesRss.CONFIG.setBaseDir(myBaseDirInput.getText());
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder((Integer)myArtistLevelInput.getValue());
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder((Integer)myAlbumLevelInput.getValue());
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

    public class BaseDirLookupButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupBaseDir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(MyTunesRss.ROOT_FRAME);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    myBaseDirInput.setText(fileChooser.getSelectedFile().getCanonicalPath());
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.lookupBaseDir") + e.getMessage());
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
        private String myPassword;

        public void focusGained(FocusEvent focusEvent) {
            myPassword = new String(myPasswordInput.getPassword()).trim();
        }

        public void focusLost(FocusEvent focusEvent) {
            String password = new String(myPasswordInput.getPassword()).trim();
            if (!myPassword.equals(password)) {
                User user = MyTunesRss.CONFIG.getUser("default");
                if (user == null) {
                    user = new User("default");
                    MyTunesRss.CONFIG.addUser(user);
                }
                user.setPassword(password);
            }
        }
    }
}