package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.UpdateTrackFileTypeStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.swing.pleasewait.PleaseWaitTask;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.settings.DataImport
 */
public class DataImport implements SettingsForm, MyTunesRssEventListener {
    private JPanel myRootPanel;
    private JTextField myArtistDropWords;
    private JTextField myId3v2CommentInput;
    private JCheckBox myIgnoreArtworkInput;
    private JCheckBox myIgnoreTimestampsInput;
    private JTable myFileTypesTable;
    private JButton myAddFileTypeButton;
    private JButton myRemoveFileTypeButton;
    private JButton myResetFileTypesButton;
    private FileTypesTableModel myFileTypesTableModel;

    public void init() {
        myAddFileTypeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myFileTypesTableModel.getFileTypes().add(new FileType(false, "", "", MediaType.Other, false));
                myFileTypesTableModel.fireTableDataChanged();
            }
        });
        myRemoveFileTypeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] rows = myFileTypesTable.getSelectedRows();
                if (rows.length > 0) {
                    for (int i = rows.length - 1; i >= 0; i--) {
                        myFileTypesTableModel.getFileTypes().remove(rows[i]);
                    }
                }
                myFileTypesTableModel.fireTableDataChanged();
            }
        });
        myResetFileTypesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                myFileTypesTableModel.setFileTypes(FileType.getDefaults());
            }
        });
        myFileTypesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                myRemoveFileTypeButton.setEnabled(myFileTypesTable.getSelectedRow() != -1);
            }
        });
        myFileTypesTableModel = new FileTypesTableModel();
        myFileTypesTable.setModel(myFileTypesTableModel);
        JComboBox mediaTypeCombo = new I18nComboBox("settings.filetypes.mediatype.Audio", "settings.filetypes.mediatype.Video", "settings.filetypes.mediatype.Image", "settings.filetypes.mediatype.Other");
        mediaTypeCombo.setOpaque(true);
        myFileTypesTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(mediaTypeCombo));
        JComboBox protectedCombo = new I18nComboBox("settings.filetypes.protected.false", "settings.filetypes.protected.true");
        protectedCombo.setOpaque(true);
        myFileTypesTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(protectedCombo));
        initValues();
    }

    public String updateConfigFromGui() {
        MyTunesRss.CONFIG.setIgnoreTimestamps(myIgnoreTimestampsInput.isSelected());
        MyTunesRss.CONFIG.setArtistDropWords(myArtistDropWords.getText());
        MyTunesRss.CONFIG.setIgnoreArtwork(myIgnoreArtworkInput.isSelected());
        MyTunesRss.CONFIG.setId3v2TrackComment(myId3v2CommentInput.getText());
        final Collection<FileType> oldTypes = MyTunesRss.CONFIG.getDeepFileTypesClone();
        MyTunesRss.CONFIG.getFileTypes().clear();
        MyTunesRss.CONFIG.getFileTypes().addAll(myFileTypesTableModel.getFileTypes());
        if (isFileTypesChanged(oldTypes, MyTunesRss.CONFIG.getFileTypes())) {
            MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.updatingTrackFileTypes"), null, false, new PleaseWaitTask() {
                public void execute() throws Exception {
                    UpdateTrackFileTypeStatement.execute(oldTypes, myFileTypesTableModel.getFileTypes());
                }
            });
        }
        return null;
    }

    private boolean isFileTypesChanged(Collection<FileType> oldTypes, List<FileType> fileTypes) {
        if (oldTypes.size() != fileTypes.size()) {
            return true;
        }
        for (FileType oldType : oldTypes) {
            for (FileType newType : fileTypes) {
                if (StringUtils.equalsIgnoreCase(oldType.getSuffix(), newType.getSuffix()) && (oldType.isProtected() != newType.isProtected() || oldType.getMediaType() != newType.getMediaType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.dataimport.title");
    }

    public void initValues() {
        myIgnoreTimestampsInput.setSelected(MyTunesRss.CONFIG.isIgnoreTimestamps());
        myArtistDropWords.setText(MyTunesRss.CONFIG.getArtistDropWords());
        myIgnoreArtworkInput.setSelected(MyTunesRss.CONFIG.isIgnoreArtwork());
        myId3v2CommentInput.setText(MyTunesRss.CONFIG.getId3v2TrackComment());
        myFileTypesTableModel.setFileTypes(MyTunesRss.CONFIG.getDeepFileTypesClone());
    }

    public void setGuiMode(GuiMode mode) {
        boolean databaseActive = DatabaseBuilderTask.isRunning() || mode == GuiMode.DatabaseUpdating;
        myIgnoreTimestampsInput.setEnabled(!databaseActive);
        SwingUtils.enableElementAndLabel(myArtistDropWords, !databaseActive);
        SwingUtils.enableElementAndLabel(myId3v2CommentInput, !databaseActive);
        myIgnoreArtworkInput.setEnabled(!databaseActive);
    }

    public void handleEvent(MyTunesRssEvent event) {
        if (event == MyTunesRssEvent.CONFIGURATION_CHANGED) {
            initValues();
        }
    }

    public class FileTypesTableModel extends AbstractTableModel {
        private List<FileType> myFileTypes;

        public FileTypesTableModel() {
            myFileTypes = new ArrayList<FileType>();
        }

        public List<FileType> getFileTypes() {
            return myFileTypes;
        }

        public void setFileTypes(List<FileType> fileTypes) {
            myFileTypes = fileTypes;
            fireTableDataChanged();
        }

        public int getRowCount() {
            return myFileTypes.size();
        }

        public int getColumnCount() {
            return 5;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            FileType type = myFileTypes.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return type.isActive();
                case 1:
                    return type.getSuffix();
                case 2:
                    return type.getMimeType();
                case 3:
                    return MyTunesRssUtils.getBundleString("settings.filetypes.mediatype." + type.getMediaType().name());
                case 4:
                    return MyTunesRssUtils.getBundleString("settings.filetypes.protected." + type.isProtected());
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
                    return Boolean.class;
                case 1:
                case 2:
                case 3:
                case 4:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            return MyTunesRssUtils.getBundleString("settings.filetypes.columnheader." + column);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < myFileTypes.size()) {
                switch (columnIndex) {
                    case 0:
                        myFileTypes.get(rowIndex).setActive((Boolean) aValue);
                        break;
                    case 1:
                        myFileTypes.get(rowIndex).setSuffix((String) aValue);
                        break;
                    case 2:
                        myFileTypes.get(rowIndex).setMimeType((String) aValue);
                        break;
                    case 3:
                        int lastDot = ((String) aValue).lastIndexOf('.');
                        myFileTypes.get(rowIndex).setMediaType(MediaType.valueOf(((String) aValue).substring(lastDot + 1)));
                        break;
                    case 4:
                        lastDot = ((String) aValue).lastIndexOf('.');
                        myFileTypes.get(rowIndex).setProtected(Boolean.valueOf(((String) aValue).substring(lastDot + 1)));
                        break;
                    default:
                        throw new IllegalArgumentException("No such column: " + columnIndex + ".");
                }
            }
        }
    }

    public class I18nComboBox extends JComboBox {
        public I18nComboBox(final String... keys) {
            super(keys);
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    return super.getListCellRendererComponent(list,
                            MyTunesRssUtils.getBundleString(value.toString()),
                            index,
                            isSelected,
                            cellHasFocus);
                }
            });
        }
    }
}