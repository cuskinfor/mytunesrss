/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.swing.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class Directories {
    private JPanel myRootPanel;
    private JTextField myTunesXmlPathInput;
    private JButton myTunesXmlPathLookupButton;
    private JTextField myBaseDirInput;
    private JButton myBaseDirLookupButton;
    private JSpinner myArtistLevelInput;
    private JSpinner myAlbumLevelInput;
    private JCheckBox myDeleteMissingFiles;
    private JTextField myUploadDirInput;
    private JButton myUploadDirLookupButton;
    private JCheckBox myCreateUserDir;

    public void init() {
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
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
        myBaseDirLookupButton.addActionListener(new BaseDirLookupButtonListener(myBaseDirInput));
        myUploadDirLookupButton.addActionListener(new BaseDirLookupButtonListener(myUploadDirInput));
        myDeleteMissingFiles.setSelected(MyTunesRss.CONFIG.isItunesDeleteMissingFiles());
        myUploadDirInput.setText(MyTunesRss.CONFIG.getUploadDir());
        myCreateUserDir.setSelected(MyTunesRss.CONFIG.isUploadCreateUserDir());
    }

    public void updateConfigFromGui() {
        MyTunesRss.CONFIG.setLibraryXml(myTunesXmlPathInput.getText().trim());
        MyTunesRss.CONFIG.setBaseDir(myBaseDirInput.getText());
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder((Integer)myArtistLevelInput.getValue());
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder((Integer)myAlbumLevelInput.getValue());
        MyTunesRss.CONFIG.setItunesDeleteMissingFiles(myDeleteMissingFiles.isSelected());
        MyTunesRss.CONFIG.setUploadDir(myUploadDirInput.getText());
        MyTunesRss.CONFIG.setUploadCreateUserDir(myCreateUserDir.isSelected());
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                SwingUtils.enableElementAndLabel(myBaseDirInput, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                myBaseDirLookupButton.setEnabled(false);
                SwingUtils.enableElementAndLabel(myAlbumLevelInput, false);
                SwingUtils.enableElementAndLabel(myArtistLevelInput, false);
                myDeleteMissingFiles.setEnabled(false);
                SwingUtils.enableElementAndLabel(myUploadDirInput, false);
                myUploadDirLookupButton.setEnabled(false);
                myCreateUserDir.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, true);
                SwingUtils.enableElementAndLabel(myBaseDirInput, true);
                myTunesXmlPathLookupButton.setEnabled(true);
                myBaseDirLookupButton.setEnabled(true);
                SwingUtils.enableElementAndLabel(myAlbumLevelInput, true);
                SwingUtils.enableElementAndLabel(myArtistLevelInput, true);
                myDeleteMissingFiles.setEnabled(true);
                SwingUtils.enableElementAndLabel(myUploadDirInput, true);
                myUploadDirLookupButton.setEnabled(true);
                myCreateUserDir.setEnabled(true);
                break;
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
        JTextField myTarget;

        public BaseDirLookupButtonListener(JTextField myTarget) {
            this.myTarget = myTarget;
        }

        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupBaseDir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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