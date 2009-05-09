/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class DataSources implements MyTunesRssEventListener, SettingsForm {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSources.class);

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
    private JButton myAddRemoteButton;
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

    public DataSources() {
        myScrollPane.setMaximumSize(myScrollPane.getPreferredSize());
        myScrollPane.getViewport().setOpaque(false);
        myBaseDirsList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = value.toString();
                if (MyTunesRssUtils.isValidRemoteUrl(text)) {
                    label.setIcon(new ImageIcon(getClass().getResource("http.gif")));
                } else if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(text), "xml")) {
                    label.setIcon(new ImageIcon(getClass().getResource("itunes.gif")));
                } else {
                    label.setIcon(new ImageIcon(getClass().getResource("folder.gif")));
                }
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
        myBaseDirsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = myBaseDirsList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < myBaseDirsList.getModel().getSize()) {
                        editDataSource(index);
                    }
                }
            }
        });
        myAddBaseDirButton.addActionListener(new AddWatchFolderButtonListener());
        myDeleteBaseDirButton.addActionListener(new DeleteWatchFolderButtonListener());
        myUploadDirLookupButton.addActionListener(new AddWatchFolderButtonListener() {
            @Override
            protected void handleChosenFile(int editIndex, File file) throws IOException {
                myUploadDirInput.setText(file.getCanonicalPath());
            }
        });
        myAddRemoteButton.addActionListener(new AddRemoteActionListener());
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
        myAddRemoteButton.setEnabled(!databaseOrServerActive);
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

    protected void editDataSource(int index) {
        if (new File(myListModel.get(index).toString()).exists()) {
            new AddWatchFolderButtonListener(index).actionPerformed(null);
        } else {
            new AddRemoteActionListener(index).actionPerformed(null);
        }
    }

    public class AddWatchFolderButtonListener implements ActionListener {
        private int myEditIndex;

        public AddWatchFolderButtonListener() {
            myEditIndex = -1;
        }

        public AddWatchFolderButtonListener(int editIndex) {
            myEditIndex = editIndex;
            myFileChooserDierctory = new File(MyTunesRss.CONFIG.getDatasources()[editIndex]).getParentFile();
        }

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
                    handleChosenFile(myEditIndex, fileChooser.getSelectedFile());
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.lookupDir", e.getMessage()));
                }
            }
        }

        protected void handleChosenFile(int editIndex, File file) throws IOException {
            String error = editIndex == -1 ? MyTunesRss.CONFIG.addDatasource(file.getCanonicalPath()) : MyTunesRss.CONFIG.replaceDatasource(editIndex, file.getCanonicalPath());
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

    public class AddRemoteActionListener implements ActionListener {
        private int myEditIndex;

        public AddRemoteActionListener() {
            myEditIndex = -1;
        }

        public AddRemoteActionListener(int editIndex) {
            myEditIndex = editIndex;
        }

        public void actionPerformed(ActionEvent e) {
            EnterTextLineDialog dialog = new EnterTextLineDialog();
            dialog.setResizable(false);
            dialog.setTitle(MyTunesRssUtils.getBundleString("dialog.title.addRemoteDataSource"));
            if (myEditIndex != -1) {
                dialog.setTextLine(myListModel.get(myEditIndex).toString());
            }
            while (true) {
                SwingUtils.packAndShowRelativeTo(dialog, myRootPanel.getParent());
                if (dialog.isCancelled()) {
                    return;
                }
                if (MyTunesRssUtils.isValidRemoteUrl(dialog.getTextLine())) {
                    break;
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.invalidRemoteUrl"));
            }
            String error = myEditIndex == -1 ? MyTunesRss.CONFIG.addDatasource(dialog.getTextLine()) : MyTunesRss.CONFIG.replaceDatasource(myEditIndex, dialog.getTextLine());
            if (error == null) {
                myListModel.clear();
                addAllToListModel();
            } else {
                MyTunesRssUtils.showErrorMessage(error);
            }
        }
    }
}