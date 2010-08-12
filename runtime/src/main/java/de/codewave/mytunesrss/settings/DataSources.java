/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.CompositeTextFieldValidation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

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
    private JTable myReplacementsTable;
    private JScrollPane myReplacementsScrollPane;
    private JButton myAddReplacementButton;
    private JButton myRemoveReplacementButton;
    private DefaultListModel myListModel;
    private File myFileChooserDierctory;
    private PathReplacementsTableModel myPathReplacementsTableModel;

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
                if (StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(text), "xml")) {
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
        myAddReplacementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myPathReplacementsTableModel.getPathReplacements().add(new PathReplacement("search expression", "replacement")); // TODO i18n
                myPathReplacementsTableModel.fireTableDataChanged();
            }
        });
        myRemoveReplacementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] rows = myReplacementsTable.getSelectedRows();
                if (rows.length > 0) {
                    for (int i = rows.length - 1; i >= 0; i--) {
                        myPathReplacementsTableModel.getPathReplacements().remove(rows[i]);
                    }
                }
                myPathReplacementsTableModel.fireTableDataChanged();
            }
        });
        myReplacementsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myRemoveReplacementButton.setEnabled(myReplacementsTable.getSelectedRow() != -1);
            }
        });
        myPathReplacementsTableModel = new PathReplacementsTableModel();
        myReplacementsTable.setModel(myPathReplacementsTableModel);
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event.getType()) {
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
        myPathReplacementsTableModel.setPathReplacements(new ArrayList<PathReplacement>(MyTunesRss.CONFIG.getPathReplacements()));
    }

    private void addAllToListModel() {
        for (String baseDir : MyTunesRss.CONFIG.getDatasources()) {
            myListModel.addElement(baseDir);
        }
    }

    public String updateConfigFromGui() {
        StringBuilder builder = new StringBuilder();
        for (PathReplacement pathReplacement : myPathReplacementsTableModel.getPathReplacements()) {
            try {
                new CompiledPathReplacement(pathReplacement);
            } catch (PatternSyntaxException e) {
                builder.append(MyTunesRssUtils.getBundleString("error.invalidPathReplacementPattern", pathReplacement.getSearchPattern())).append(" ");
            }
        }
        if (builder.length() == 0) {
            MyTunesRss.CONFIG.setAlbumFallback(myAlbumFallbackInput.getText());
            MyTunesRss.CONFIG.setArtistFallback(myArtistFallbackInput.getText());
            MyTunesRss.CONFIG.setUploadDir(myUploadDirInput.getText());
            MyTunesRss.CONFIG.setUploadCreateUserDir(myCreateUserDir.isSelected());
            MyTunesRss.CONFIG.clearPathReplacements();
            for (PathReplacement pathReplacement : myPathReplacementsTableModel.getPathReplacements()) {
                MyTunesRss.CONFIG.addPathReplacement(pathReplacement);
            }
        }
        return StringUtils.trimToNull(builder.toString());
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
        myReplacementsScrollPane.setEnabled(!databaseOrServerActive);
        myReplacementsTable.setEnabled(!databaseOrServerActive);
        myAddReplacementButton.setEnabled(!databaseOrServerActive);
        myRemoveReplacementButton.setEnabled(!databaseOrServerActive && myReplacementsTable.getSelectedRow() != -1);
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.directories.title");
    }

    protected void editDataSource(int index) {
        if (new File(myListModel.get(index).toString()).exists()) {
            new AddWatchFolderButtonListener(index).actionPerformed(null);
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

    public class PathReplacementsTableModel extends AbstractTableModel {
        private java.util.List<PathReplacement> myPathReplacements;

        public PathReplacementsTableModel() {
            myPathReplacements = new ArrayList<PathReplacement>();
        }

        public java.util.List<PathReplacement> getPathReplacements() {
            return myPathReplacements;
        }

        public void setPathReplacements(java.util.List<PathReplacement> pathReplacements) {
            myPathReplacements = pathReplacements;
            fireTableDataChanged();
        }

        public int getRowCount() {
            return myPathReplacements.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            PathReplacement replacement = myPathReplacements.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return replacement.getSearchPattern();
                case 1:
                    return replacement.getReplacement();
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            return MyTunesRssUtils.getBundleString("settings.pathReplacements.columnheader." + column);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < myPathReplacements.size()) {
                switch (columnIndex) {
                    case 0:
                        myPathReplacements.get(rowIndex).setSearchPattern((String) aValue);
                        break;
                    case 1:
                        myPathReplacements.get(rowIndex).setReplacement((String) aValue);
                        break;
                    default:
                        throw new IllegalArgumentException("No such column: " + columnIndex + ".");
                }
            }
        }
    }
}