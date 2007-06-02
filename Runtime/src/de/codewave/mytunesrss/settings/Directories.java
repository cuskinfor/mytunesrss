/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.*;
import org.apache.commons.io.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
    private JPanel myUploadPanel;
    private DefaultListModel myListModel = new DefaultListModel();

    private void createUIComponents() {
        myBaseDirsList = new JList() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(0, 0);
            }
        };
    }

    public void init() {
        initRegistration();
        myScrollPane.setMaximumSize(myScrollPane.getPreferredSize());
        myScrollPane.getViewport().setOpaque(false);
        myFolderStructureGrandparent.addItem(FolderStructureRole.None);
        myFolderStructureGrandparent.addItem(FolderStructureRole.Album);
        myFolderStructureGrandparent.addItem(FolderStructureRole.Artist);
        myFolderStructureParent.addItem(FolderStructureRole.None);
        myFolderStructureParent.addItem(FolderStructureRole.Album);
        myFolderStructureParent.addItem(FolderStructureRole.Artist);
        addAllToListModel();
        myBaseDirsList.setModel(myListModel);
        myBaseDirsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myDeleteBaseDirButton.setEnabled(myBaseDirsList.getSelectedIndex() > -1);
            }
        });
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemArtistNameFolder(), FolderStructureRole.Artist);
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemAlbumNameFolder(), FolderStructureRole.Album);
        myAddBaseDirButton.addActionListener(new AddWatchFolderButtonListener());
        myDeleteBaseDirButton.addActionListener(new DeleteWatchFolderButtonListener());
        myUploadDirLookupButton.addActionListener(new AddWatchFolderButtonListener() {
            @Override
            protected void handleChosenFile(File file) throws IOException {
                myUploadDirInput.setText(file.getCanonicalPath());
            }
        });
        myUploadDirInput.setText(MyTunesRss.CONFIG.getUploadDir());
        myCreateUserDir.setSelected(MyTunesRss.CONFIG.isUploadCreateUserDir());
    }

    private void initRegistration() {
        myUploadPanel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
    }

    private void addAllToListModel() {
        for (String baseDir : MyTunesRss.CONFIG.getWatchFolders()) {
            myListModel.addElement(baseDir);
        }
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
        MyTunesRss.CONFIG.setFileSystemArtistNameFolder(getFolderStructureRole(FolderStructureRole.Artist));
        MyTunesRss.CONFIG.setFileSystemAlbumNameFolder(getFolderStructureRole(FolderStructureRole.Album));
        MyTunesRss.CONFIG.setUploadDir(myUploadDirInput.getText());
        MyTunesRss.CONFIG.setUploadCreateUserDir(myCreateUserDir.isSelected());
        return null;
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myBaseDirsList, false);
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
                SwingUtils.enableElementAndLabel(myBaseDirsList, true);
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

    public class AddWatchFolderButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (MyTunesRss.REGISTRATION.isRegistered() || MyTunesRss.CONFIG.getWatchFolders().length < MyTunesRssRegistration.UNREGISTERED_MAX_WATCHFOLDERS) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle(MyTunesRss.BUNDLE.getString("dialog.lookupBaseDir"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory() || (file.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(file.getName())));
                    }

                    public String getDescription() {
                        return null;
                    }
                });
                int result = fileChooser.showOpenDialog(MyTunesRss.ROOT_FRAME);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        handleChosenFile(fileChooser.getSelectedFile());
                    } catch (IOException e) {
                        MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.lookupBaseDir") + e.getMessage());
                    }
                }
            } else {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.unregisteredMaxWatchFolders", MyTunesRssRegistration.UNREGISTERED_MAX_WATCHFOLDERS));
            }
        }

        protected void handleChosenFile(File file) throws IOException {
            String error = MyTunesRss.CONFIG.addWatchFolder(file.getCanonicalPath());
            if (error == null) {
                myListModel.clear();
                addAllToListModel();
            } else {
                MyTunesRssUtils.showErrorMessage(error);
            }
        }
    }

    public class DeleteWatchFolderButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = myBaseDirsList.getSelectedIndex();
            if (index > -1 && index < myListModel.getSize()) {
                String error = MyTunesRss.CONFIG.removeWatchFolder(myListModel.get(index).toString());
                if (error == null) {
                    myListModel.remove(index);
                } else {
                    MyTunesRssUtils.showErrorMessage(error);
                }
            }
        }
    }
}