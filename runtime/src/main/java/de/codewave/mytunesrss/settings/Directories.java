/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class Directories implements MyTunesRssEventListener, SettingsForm {

    private JPanel myRootPanel;
    private JList myBaseDirsList;
    private JButton myAddBaseDirButton;
    private JTextField myUploadDirInput;
    private JButton myUploadDirLookupButton;
    private JCheckBox myCreateUserDir;
    private JButton myDeleteBaseDirButton;
    private JScrollPane myScrollPane;
    private JTextField myArtistFallbackInput;
    private JTextField myAlbumFallbackInput;
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
        myScrollPane.setMaximumSize(myScrollPane.getPreferredSize());
        myScrollPane.getViewport().setOpaque(false);
        initValues();
        myBaseDirsList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = value.toString();
                label.setIcon(new ImageIcon(getClass().getResource(
                        "xml".equalsIgnoreCase(FilenameUtils.getExtension(text)) ? "itunes.gif" : "folder.gif")));
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

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                    case DATABASE_UPDATE_STATE_CHANGED:
                        setGuiMode(GuiMode.DatabaseUpdating);
                        break;
                    case DATABASE_UPDATE_FINISHED:
                    case DATABASE_UPDATE_FINISHED_NOT_RUN:
                        setGuiMode(GuiMode.DatabaseIdle);
                        break;
                    case SERVER_STARTED:
                        setGuiMode(GuiMode.ServerRunning);
                        break;
                    case SERVER_STOPPED:
                        setGuiMode(GuiMode.ServerIdle);
                        break;
                }
            }
        });
    }

    public void initValues() {
        myListModel = new DefaultListModel();
        addAllToListModel();
        myBaseDirsList.setModel(myListModel);
        myAlbumFallbackInput.setText(MyTunesRss.CONFIG.getAlbumFallback());
        myArtistFallbackInput.setText(MyTunesRss.CONFIG.getArtistFallback());
        myUploadDirInput.setText(MyTunesRss.CONFIG.getUploadDir());
        myCreateUserDir.setSelected(MyTunesRss.CONFIG.isUploadCreateUserDir());
    }

    private void addAllToListModel() {
        for (String baseDir : MyTunesRss.CONFIG.getDatasources()) {
            myListModel.addElement(baseDir);
        }
    }

    public String updateConfigFromGui() {
        MyTunesRss.CONFIG.setAlbumFallback(myAlbumFallbackInput.getText());
        MyTunesRss.CONFIG.setArtistFallback(myArtistFallbackInput.getText());
        MyTunesRss.CONFIG.setUploadDir(myUploadDirInput.getText());
        MyTunesRss.CONFIG.setUploadCreateUserDir(myCreateUserDir.isSelected());
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void setGuiMode(GuiMode mode) {
        boolean databaseOrServerActive = DatabaseBuilderTask.isRunning() || MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.DatabaseUpdating ||
                mode == GuiMode.ServerRunning;
        myBaseDirsList.setEnabled(!databaseOrServerActive);
        myAddBaseDirButton.setEnabled(!databaseOrServerActive);
        myDeleteBaseDirButton.setEnabled(!databaseOrServerActive && myBaseDirsList.getSelectedIndex() > -1);
        myUploadDirInput.setEnabled(!databaseOrServerActive);
        myUploadDirLookupButton.setEnabled(!databaseOrServerActive);
        myCreateUserDir.setEnabled(!databaseOrServerActive);
        SwingUtils.enableElementAndLabel(myAlbumFallbackInput, !databaseOrServerActive);
        SwingUtils.enableElementAndLabel(myArtistFallbackInput, !databaseOrServerActive);
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.directories.title");
    }

    public class AddWatchFolderButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setCurrentDirectory(myFileChooserDierctory);
            fileChooser.setDialogTitle(MyTunesRssUtils.getBundleString("dialog.lookupBaseDir"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || (file.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(file.getName())));
                }

                public String getDescription() {
                    return MyTunesRssUtils.getBundleString("filechooser.filter.watchfolder");
                }
            });
            int result = fileChooser.showDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("filechooser.approve.watchfolder"));
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    myFileChooserDierctory = fileChooser.getCurrentDirectory();
                    handleChosenFile(fileChooser.getSelectedFile());
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.lookupDir", e.getMessage()));
                }
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