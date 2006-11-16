/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

/**
 * General settings panel
 */
public class General {
    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
    private JLabel myServerStatusLabel;
    private JButton myServerInfoButton;
    private JTextField myBaseDirInput;
    private JButton myBaseDirLookupButton;
    private JSpinner myArtistLevelInput;
    private JSpinner myAlbumLevelInput;

    public JTextField getPortInput() {
        return myPortInput;
    }

    public JTextField getTunesXmlPathInput() {
        return myTunesXmlPathInput;
    }

    public void init() {
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myBaseDirInput.setText(MyTunesRss.CONFIG.getBaseDir());
        int artistValue = MyTunesRss.CONFIG.getFileSystemArtistNameFolder();
        if (artistValue < 0) {
            artistValue = 0;
        } else if (artistValue > 5) {
            artistValue = 5;
        }
        SpinnerNumberModel artistModel = new SpinnerNumberModel(artistValue, 0, 5, 1);
        int albumValue = MyTunesRss.CONFIG.getFileSystemAlbumNameFolder();
        if (albumValue < 0) {
            albumValue = 0;
        } else if (albumValue > 5) {
            albumValue = 5;
        }
        SpinnerNumberModel albumModel = new SpinnerNumberModel(albumValue, 0, 5, 1);
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
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                SwingUtils.enableElementAndLabel(myBaseDirInput, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                myBaseDirLookupButton.setEnabled(false);
                SwingUtils.enableElementAndLabel(myAlbumLevelInput, false);
                SwingUtils.enableElementAndLabel(myArtistLevelInput, false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myPortInput, true);
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, true);
                SwingUtils.enableElementAndLabel(myBaseDirInput, true);
                myTunesXmlPathLookupButton.setEnabled(true);
                myBaseDirLookupButton.setEnabled(true);
                SwingUtils.enableElementAndLabel(myAlbumLevelInput, true);
                SwingUtils.enableElementAndLabel(myArtistLevelInput, true);
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
}