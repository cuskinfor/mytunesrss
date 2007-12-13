/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import org.apache.commons.io.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class Directories implements MyTunesRssEventListener {
    public enum FolderStructureRole {
        Artist, Album, None;

        public String toString() {
            switch (this) {
                case Album:
                    return MyTunesRssUtils.getBundleString("settings.folderStructureRoleAlbum");
                case Artist:
                    return MyTunesRssUtils.getBundleString("settings.folderStructureRoleArtist");
                default:
                    return MyTunesRssUtils.getBundleString("settings.folderStructureRoleNone");
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
    private DefaultListModel myListModel;
    private File myFileChooserDierctory;

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
        initValues();
        myBaseDirsList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = value.toString();
                label.setIcon(new ImageIcon(getClass().getResource("xml".equalsIgnoreCase(FilenameUtils.getExtension(text)) ? "itunes.gif" : "folder.gif")));
                label.setText(text);
                label.setToolTipText(text);
                return label;
            }
        });
        myBaseDirsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myDeleteBaseDirButton.setEnabled(myBaseDirsList.getSelectedIndex() > -1);
            }
        });
        myAddBaseDirButton.addActionListener(new AddWatchFolderButtonListener());
        myDeleteBaseDirButton.addActionListener(new DeleteWatchFolderButtonListener());
        myUploadDirLookupButton.addActionListener(new AddWatchFolderButtonListener() {
            @Override
            protected void handleChosenFile(File file) throws IOException {
                myUploadDirInput.setText(file.getCanonicalPath());
            }
        });
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void handleEvent(MyTunesRssEvent event) {
        if (event == MyTunesRssEvent.CONFIGURATION_CHANGED) {
            initValues();
        }
    }

    private void initValues() {
        myListModel = new DefaultListModel();
        addAllToListModel();
        myBaseDirsList.setModel(myListModel);
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemArtistNameFolder(), FolderStructureRole.Artist);
        setFolderStructureRole(MyTunesRss.CONFIG.getFileSystemAlbumNameFolder(), FolderStructureRole.Album);
        myUploadDirInput.setText(MyTunesRss.CONFIG.getUploadDir());
        myCreateUserDir.setSelected(MyTunesRss.CONFIG.isUploadCreateUserDir());
    }

    private void initRegistration() {
        myUploadPanel.setVisible(MyTunesRss.REGISTRATION.isRegistered());
    }

    private void addAllToListModel() {
        for (String baseDir : MyTunesRss.CONFIG.getDatasources()) {
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
                myBaseDirsList.setEnabled(false);
                myAddBaseDirButton.setEnabled(false);
                myDeleteBaseDirButton.setEnabled(false);
                myFolderStructureGrandparent.setEnabled(false);
                myFolderStructureParent.setEnabled(false);
                myUploadDirInput.setEnabled(false);
                myUploadDirLookupButton.setEnabled(false);
                myCreateUserDir.setEnabled(false);
                myStructureLabel.setEnabled(false);
                mySeparatorLabel1.setEnabled(false);
                mySeparatorLabel2.setEnabled(false);
                myTrackLabel.setEnabled(false);
                break;
            case ServerIdle:
                myBaseDirsList.setEnabled(true);
                myAddBaseDirButton.setEnabled(true);
                myDeleteBaseDirButton.setEnabled(myBaseDirsList.getSelectedIndex() > -1);
                myFolderStructureGrandparent.setEnabled(true);
                myFolderStructureParent.setEnabled(true);
                myUploadDirInput.setEnabled(true);
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
            if (MyTunesRss.REGISTRATION.isRegistered() || MyTunesRss.CONFIG.getDatasources().length < MyTunesRssRegistration.UNREGISTERED_MAX_WATCHFOLDERS) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(myFileChooserDierctory);
                fileChooser.setDialogTitle(MyTunesRssUtils.getBundleString("dialog.lookupBaseDir"));
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
                        myFileChooserDierctory = fileChooser.getCurrentDirectory();
                        handleChosenFile(fileChooser.getSelectedFile());
                    } catch (IOException e) {
                        MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.lookupDir", e.getMessage()));
                    }
                }
            } else {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.unregisteredMaxDataSources", MyTunesRssRegistration.UNREGISTERED_MAX_WATCHFOLDERS));
            }
        }

        protected void handleChosenFile(File file) throws IOException {
            String error = MyTunesRss.CONFIG.addDatasource(file.getCanonicalPath());
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
                String error = MyTunesRss.CONFIG.removeDatasource(myListModel.get(index).toString());
                if (error == null) {
                    myListModel.remove(index);
                } else {
                    MyTunesRssUtils.showErrorMessage(error);
                }
            }
        }
    }
}