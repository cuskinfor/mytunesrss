/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.intellij.uiDesigner.core.*;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class Directories {

    public enum FolderStructureRole {
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
    private JList myBaseDirsList;
    private JButton myAddBaseDirButton;
    private JTextField myUploadDirInput;
    private JButton myUploadDirLookupButton;
    private JCheckBox myCreateUserDir;
    private JComboBox myFolderStructureGrandparent;
    private JComboBox myFolderStructureParent;
    private JLabel myStructureLabel;
    private JLabel mySeparatorLabel1;
    private JLabel mySeparatorLabel2;
    private JLabel myTrackLabel;
    private JButton myDeleteBaseDirButton;
    private JScrollPane myScrollPane;
    private DefaultListModel myListModel = new DefaultListModel();

    private void createUIComponents() {
        myBaseDirsList = new JList() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                if (myScrollPane != null) {
                    Dimension size = myScrollPane.getViewport().getSize();
                    Insets insets = myScrollPane.getViewport().getInsets();
                    size.width -= (insets.left + insets.right);
                    size.height -= (insets.top + insets.bottom);
                }
                return new Dimension(0, 0);
            }
        };
    }

    public void init() {
        myScrollPane.setMaximumSize(myScrollPane.getPreferredSize());
        myScrollPane.getViewport().setOpaque(false);
        myFolderStructureGrandparent.addItem(FolderStructureRole.None);
        myFolderStructureGrandparent.addItem(FolderStructureRole.Album);
        myFolderStructureGrandparent.addItem(FolderStructureRole.Artist);
        myFolderStructureParent.addItem(FolderStructureRole.None);
        myFolderStructureParent.addItem(FolderStructureRole.Album);
        myFolderStructureParent.addItem(FolderStructureRole.Artist);
        myTunesXmlPathLookupButton.addActionListener(new TunesXmlPathLookupButtonListener());
        myTunesXmlPathInput.setText(MyTunesRss.CONFIG.getLibraryXml());
        myListModel.addElement(MyTunesRss.CONFIG.getBaseDir());
        myBaseDirsList.setModel(myListModel);
        myBaseDirsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myDeleteBaseDirButton.setEnabled(myBaseDirsList.getSelectedIndex() > -1);
            }
        });
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemArtistNameFolder(), FolderStructureRole.Artist);
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemAlbumNameFolder(), FolderStructureRole.Album);
        myAddBaseDirButton.addActionListener(new AddBaseDirButtonListener());
        myDeleteBaseDirButton.addActionListener(new DeleteBaseDirButtonListener());
        myUploadDirLookupButton.addActionListener(new AddBaseDirButtonListener() {
            @Override
            protected void handleChosenFile(File file) throws IOException {
                myUploadDirInput.setText(file.getCanonicalPath());
            }
        });
        myUploadDirInput.setText(MyTunesRss.CONFIG.getUploadDir());
        myCreateUserDir.setSelected(MyTunesRss.CONFIG.isUploadCreateUserDir());
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

    public String updateConfigFromGui() {
        MyTunesRss.CONFIG.setLibraryXml(myTunesXmlPathInput.getText().trim());
        MyTunesRss.CONFIG.setBaseDir(myListModel.get(0).toString());
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(getFolderStructureRole(FolderStructureRole.Artist));
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(getFolderStructureRole(FolderStructureRole.Album));
        MyTunesRss.CONFIG.setUploadDir(myUploadDirInput.getText());
        MyTunesRss.CONFIG.setUploadCreateUserDir(myCreateUserDir.isSelected());
        return null;
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, false);
                SwingUtils.enableElementAndLabel(myBaseDirsList, false);
                myTunesXmlPathLookupButton.setEnabled(false);
                myAddBaseDirButton.setEnabled(false);
                myDeleteBaseDirButton.setEnabled(false);
                myFolderStructureGrandparent.setEnabled(false);
                myFolderStructureParent.setEnabled(false);
                SwingUtils.enableElementAndLabel(myUploadDirInput, false);
                myUploadDirLookupButton.setEnabled(false);
                myCreateUserDir.setEnabled(false);
                myStructureLabel.setEnabled(false);
                mySeparatorLabel1.setEnabled(false);
                mySeparatorLabel2.setEnabled(false);
                myTrackLabel.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myTunesXmlPathInput, true);
                SwingUtils.enableElementAndLabel(myBaseDirsList, true);
                myTunesXmlPathLookupButton.setEnabled(true);
                myAddBaseDirButton.setEnabled(true);
                myDeleteBaseDirButton.setEnabled(myBaseDirsList.getSelectedIndex() > -1);
                myFolderStructureGrandparent.setEnabled(true);
                myFolderStructureParent.setEnabled(true);
                SwingUtils.enableElementAndLabel(myUploadDirInput, true);
                myUploadDirLookupButton.setEnabled(true);
                myCreateUserDir.setEnabled(true);
                myStructureLabel.setEnabled(true);
                mySeparatorLabel1.setEnabled(true);
                mySeparatorLabel2.setEnabled(true);
                myTrackLabel.setEnabled(true);
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

    public class AddBaseDirButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupBaseDir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(MyTunesRss.ROOT_FRAME);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    handleChosenFile(fileChooser.getSelectedFile());
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.lookupBaseDir") + e.getMessage());
                }
            }
        }

        protected void handleChosenFile(File file) throws IOException {
            myListModel.addElement(file.getCanonicalPath());
        }
    }

    public class DeleteBaseDirButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = myBaseDirsList.getSelectedIndex();
            if (index > -1 && index < myListModel.getSize()) {
                myListModel.remove(index);
            }
        }
    }
}