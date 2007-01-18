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

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class Directories {
    private enum FolderStructureRole {
        Artist, Album, None;

        public String toString() {
            switch (this) {
                case Album:
                    return MyTunesRss.BUNDLE.getString("settings.folderStructureRoleAlbum");
                case Artist:
                    return MyTunesRss.BUNDLE.getString("settings.folderStructureRoleArtist");
                default:
                    return MyTunesRss.BUNDLE.getString("settings.folderStructureRoleNone");
            }
        }
    }

    private JPanel myRootPanel;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
    private JTextField myBaseDirInput;
    private JButton myBaseDirLookupButton;
    private JCheckBox myDeleteMissingFiles;
    private JTextField myUploadDirInput;
    private JButton myUploadDirLookupButton;
    private JCheckBox myCreateUserDir;
    private JComboBox myFolderStructureGrandparent;
    private JComboBox myFolderStructureParent;
    private JLabel myStructureLabel;
    private JLabel mySeparatorLabel1;
    private JLabel mySeparatorLabel2;
    private JLabel myTrackLabel;
    private JTextField myFileTypes;
    private JTextField myArtistDropWords;

    public void init() {
        myFolderStructureGrandparent.addItem(FolderStructureRole.None);
        myFolderStructureGrandparent.addItem(FolderStructureRole.Album);
        myFolderStructureGrandparent.addItem(FolderStructureRole.Artist);
        myFolderStructureParent.addItem(FolderStructureRole.None);
        myFolderStructureParent.addItem(FolderStructureRole.Album);
        myFolderStructureParent.addItem(FolderStructureRole.Artist);
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myBaseDirInput.setText(MyTunesRss.CONFIG.getBaseDir());
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemArtistNameFolder(), FolderStructureRole.Artist);
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemAlbumNameFolder(), FolderStructureRole.Album);
        myBaseDirLookupButton.addActionListener(new BaseDirLookupButtonListener(myBaseDirInput));
        myUploadDirLookupButton.addActionListener(new BaseDirLookupButtonListener(myUploadDirInput));
        myDeleteMissingFiles.setSelected(MyTunesRss.CONFIG.isItunesDeleteMissingFiles());
        myUploadDirInput.setText(MyTunesRss.CONFIG.getUploadDir());
        myCreateUserDir.setSelected(MyTunesRss.CONFIG.isUploadCreateUserDir());
        myFileTypes.setText(MyTunesRss.CONFIG.getFileTypes());
        myArtistDropWords.setText(MyTunesRss.CONFIG.getArtistDropWords());
    }

    private void setFolderStructureRole(int level, FolderStructureRole role) {
        if (level == 1) {
            myFolderStructureParent.setSelectedItem(role);
        } else if (level == 2) {
            myFolderStructureGrandparent.setSelectedItem(role);
        }
    }

    private int getFolderStructureRole(FolderStructureRole role) {
        if (myFolderStructureGrandparent.getSelectedItem().equals(role)) {
            return 2;
        } else if (myFolderStructureParent.getSelectedItem().equals(role)) {
            return 1;
        }
        return 0;
    }

    public void updateConfigFromGui() {
        MyTunesRss.CONFIG.setLibraryXml(myTunesXmlPathInput.getText().trim());
        MyTunesRss.CONFIG.setBaseDir(myBaseDirInput.getText());
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(getFolderStructureRole(FolderStructureRole.Artist));
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(getFolderStructureRole(FolderStructureRole.Album));
        MyTunesRss.CONFIG.setItunesDeleteMissingFiles(myDeleteMissingFiles.isSelected());
        MyTunesRss.CONFIG.setUploadDir(myUploadDirInput.getText());
        MyTunesRss.CONFIG.setUploadCreateUserDir(myCreateUserDir.isSelected());
        MyTunesRss.CONFIG.setFileTypes(myFileTypes.getText());
        MyTunesRss.CONFIG.setArtistDropWords(myArtistDropWords.getText());
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                SwingUtils.enableElementAndLabel(myBaseDirInput, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                myBaseDirLookupButton.setEnabled(false);
                myFolderStructureGrandparent.setEnabled(false);
                myFolderStructureParent.setEnabled(false);
                myDeleteMissingFiles.setEnabled(false);
                SwingUtils.enableElementAndLabel(myUploadDirInput, false);
                myUploadDirLookupButton.setEnabled(false);
                myCreateUserDir.setEnabled(false);
                myStructureLabel.setEnabled(false);
                mySeparatorLabel1.setEnabled(false);
                mySeparatorLabel2.setEnabled(false);
                myTrackLabel.setEnabled(false);
                SwingUtils.enableElementAndLabel(myFileTypes, false);
                SwingUtils.enableElementAndLabel(myArtistDropWords, false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, true);
                SwingUtils.enableElementAndLabel(myBaseDirInput, true);
                myTunesXmlPathLookupButton.setEnabled(true);
                myBaseDirLookupButton.setEnabled(true);
                myFolderStructureGrandparent.setEnabled(true);
                myFolderStructureParent.setEnabled(true);
                myDeleteMissingFiles.setEnabled(true);
                SwingUtils.enableElementAndLabel(myUploadDirInput, true);
                myUploadDirLookupButton.setEnabled(true);
                myCreateUserDir.setEnabled(true);
                myStructureLabel.setEnabled(true);
                mySeparatorLabel1.setEnabled(true);
                mySeparatorLabel2.setEnabled(true);
                myTrackLabel.setEnabled(true);
                SwingUtils.enableElementAndLabel(myFileTypes, true);
                SwingUtils.enableElementAndLabel(myArtistDropWords, true);
                break;
        }
    }

    public class TunesXmlPathLookupButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            FileDialog fileDialog = new FileDialog(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.loadITunes"), FileDialog.LOAD);
            if (StringUtils.isNotEmpty(myTunesXmlPathInput.getText())) {
                fileDialog.setFile(myTunesXmlPathInput.getText());
            }
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
        JTextField myTarget;

        public BaseDirLookupButtonListener(JTextField myTarget) {
            this.myTarget = myTarget;
        }

        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupBaseDir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (StringUtils.isNotEmpty(myTarget.getText())) {
                fileChooser.setCurrentDirectory(new File(myTarget.getText()));
            }
            int result = fileChooser.showOpenDialog(MyTunesRss.ROOT_FRAME);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    myTarget.setText(fileChooser.getSelectedFile().getCanonicalPath());
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.lookupBaseDir") + e.getMessage());
                }
            }
        }
    }
}