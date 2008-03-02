/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.swing.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.settings.Database
 */
public class Database implements MyTunesRssEventListener {
    private static final Log LOG = LogFactory.getLog(Database.class);

    private JPanel myRootPanel;
    private JLabel myLastUpdatedLabel;
    private JCheckBox myIgnoreTimestampsInput;
    private JButton myDeleteDatabaseButton;
    private JButton myUpdateDatabaseButton;
    private JTextField myArtistDropWords;
    private JTextField myFileTypes;
    private JCheckBox myUpdateDatabaseOnServerStart;
    private JCheckBox myDeleteMissingFiles;
    private JCheckBox myIgnoreArtworkInput;
    private JScrollPane myScrollPane;
    private JPanel mySchedulePanel;
    private DeleteTriggerActionListener myDeleteTriggerActionListener = new DeleteTriggerActionListener();

    public void init() {
        myScrollPane.getViewport().setOpaque(false);
        refreshTriggers();
        refreshLastUpdate();
        myDeleteDatabaseButton.addActionListener(new DeleteDatabaseButtonListener());
        myUpdateDatabaseButton.addActionListener(new UpdateDatabaseButtonListener());
        initValues();
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    private void refreshTriggers() {
        mySchedulePanel.removeAll();
        List<String> triggers = new ArrayList<String>(MyTunesRss.CONFIG.getDatabaseCronTriggers());
        mySchedulePanel.setLayout(new GridLayoutManager(triggers.size() + 2, 4));
        int row = 0;
        for (String trigger : triggers) {
            addTrigger(trigger, row++);
        }
        JButton addButton = new JButton(MyTunesRssUtils.getBundleString("settings.newTrigger"));
        addButton.setToolTipText(MyTunesRssUtils.getBundleString("settings.newTriggerTooltip"));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MyTunesRss.CONFIG.getDatabaseCronTriggers().add("0 0 0 * * MON-SUN");
                refreshTriggers();
            }
        });
        addPanelComponent(addButton, new GridConstraints(row++,
                                                         0,
                                                         1,
                                                         4,
                                                         GridConstraints.ANCHOR_WEST,
                                                         GridConstraints.FILL_NONE,
                                                         GridConstraints.SIZEPOLICY_FIXED,
                                                         GridConstraints.SIZEPOLICY_FIXED,
                                                         null,
                                                         null,
                                                         null));
        addPanelComponent(new JLabel(""), new GridConstraints(row++,
                                                              0,
                                                              1,
                                                              4,
                                                              GridConstraints.ANCHOR_WEST,
                                                              GridConstraints.FILL_BOTH,
                                                              GridConstraints.SIZEPOLICY_WANT_GROW,
                                                              GridConstraints.SIZEPOLICY_WANT_GROW,
                                                              null,
                                                              null,
                                                              null));
        mySchedulePanel.validate();
    }

    private void addTrigger(String triggerText, int row) {
        String[] triggerParts = triggerText.split(" ");
        JComboBox comboBox = new JComboBox(getDays());
        comboBox.setSelectedItem(new TriggerItem(triggerParts[5], MyTunesRssUtils.getBundleString("settings.cron.day." + triggerParts[5])));
        comboBox.addItemListener(new ChangeTriggerActionListener(row, 5));
        addPanelComponent(comboBox, createConstraints(row, 0));
        comboBox = new JComboBox(getHours());
        comboBox.setSelectedItem(new TriggerItem(triggerParts[2], triggerParts[2]));
        comboBox.addItemListener(new ChangeTriggerActionListener(row, 2));
        addPanelComponent(comboBox, createConstraints(row, 1));
        comboBox = new JComboBox(getMinutes());
        comboBox.setSelectedItem(new TriggerItem(triggerParts[1], triggerParts[1]));
        comboBox.addItemListener(new ChangeTriggerActionListener(row, 1));
        addPanelComponent(comboBox, createConstraints(row, 2));
        JButton delete = new JButton(MyTunesRssUtils.getBundleString("settings.deleteTrigger"));
        delete.setToolTipText(MyTunesRssUtils.getBundleString("settings.deleteTriggerTooltip"));
        delete.setActionCommand(Integer.toString(row));
        delete.setOpaque(false);
        delete.addActionListener(myDeleteTriggerActionListener);
        addPanelComponent(delete, createConstraints(row, 3));
    }

    private TriggerItem[] getDays() {
        String[] keys = new String[] {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN", "MON-FRI", "MON-SUN", "SAT-SUN"};
        TriggerItem[] items = new TriggerItem[keys.length];
        for (int i = 0; i < keys.length; i++) {
            items[i] = new TriggerItem(keys[i], MyTunesRssUtils.getBundleString("settings.cron.day." + keys[i]));
        }
        return items;
    }

    private TriggerItem[] getHours() {
        TriggerItem[] values = new TriggerItem[25];
        values[24] = new TriggerItem("0/1", "00/01");
        for (int i = 0; i < 24; i++) {
            String key = (i < 10 ? "0" : "") + Integer.toString(i);
            values[i] = new TriggerItem(key, key);
        }
        return values;
    }

    private TriggerItem[] getMinutes() {
        TriggerItem[] values = new TriggerItem[16];
        values[12] = new TriggerItem("0/5", "00/05");
        values[13] = new TriggerItem("0/10", "00/10");
        values[14] = new TriggerItem("0/15", "00/15");
        values[15] = new TriggerItem("0/20", "00/20");
        for (int i = 0; i < 60; i += 5) {
            String key = Integer.toString(i);
            String value = (i < 10 ? "0" : "") + Integer.toString(i);
            values[i / 5] = new TriggerItem(key, value);
        }
        return values;
    }

    private GridConstraints createConstraints(int row, int column) {
        return new GridConstraints(row,
                                   column,
                                   1,
                                   1,
                                   GridConstraints.ANCHOR_WEST,
                                   GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED,
                                   null,
                                   null,
                                   null);
    }

    private void addPanelComponent(JComponent component, GridConstraints gridConstraints) {
        mySchedulePanel.add(component, gridConstraints);
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event) {
                    case CONFIGURATION_CHANGED:
                        initValues();
                        break;
                    case DATABASE_UPDATE_STATE_CHANGED:
                        myLastUpdatedLabel.setText(MyTunesRssUtils.getBundleString(event.getMessageKey()));
                        setGuiMode(GuiMode.DatabaseUpdating);
                        break;
                    case DATABASE_UPDATE_FINISHED:
                        refreshLastUpdate();
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

    private void initValues() {
        myUpdateDatabaseOnServerStart.setSelected(MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart());
        myIgnoreTimestampsInput.setSelected(MyTunesRss.CONFIG.isIgnoreTimestamps());
        myDeleteMissingFiles.setSelected(MyTunesRss.CONFIG.isItunesDeleteMissingFiles());
        myFileTypes.setText(MyTunesRss.CONFIG.getFileTypes());
        myArtistDropWords.setText(MyTunesRss.CONFIG.getArtistDropWords());
        myIgnoreArtworkInput.setSelected(MyTunesRss.CONFIG.isIgnoreArtwork());
    }

    public void refreshLastUpdate() {
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            final SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (systemInformation.getLastUpdate() > 0) {
                        Date date = new Date(systemInformation.getLastUpdate());
                        myLastUpdatedLabel.setText(MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(
                                MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdateDateFormat")).format(date));
                    } else {
                        myLastUpdatedLabel.setText(MyTunesRssUtils.getBundleString("settings.databaseNotYetCreated"));
                    }
                    myRootPanel.validate();
                }
            });
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get last update time from database.", e);
            }

        } finally {
            session.commit();
        }
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setUpdateDatabaseOnServerStart(myUpdateDatabaseOnServerStart.isSelected());
            MyTunesRss.CONFIG.setIgnoreTimestamps(myIgnoreTimestampsInput.isSelected());
            MyTunesRss.CONFIG.setItunesDeleteMissingFiles(myDeleteMissingFiles.isSelected());
            MyTunesRss.CONFIG.setFileTypes(myFileTypes.getText());
            MyTunesRss.CONFIG.setArtistDropWords(myArtistDropWords.getText());
            MyTunesRss.CONFIG.setIgnoreArtwork(myIgnoreArtworkInput.isSelected());
        }
        return null;
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        boolean databaseActive = DatabaseBuilderTask.isRunning() || mode == GuiMode.DatabaseUpdating;
        myUpdateDatabaseOnServerStart.setEnabled(!serverActive);
        myIgnoreTimestampsInput.setEnabled(!databaseActive);
        myUpdateDatabaseButton.setEnabled(!databaseActive);
        myDeleteDatabaseButton.setEnabled(!databaseActive);
        myDeleteMissingFiles.setEnabled(!databaseActive);
        SwingUtils.enableElementAndLabel(myFileTypes, !databaseActive);
        SwingUtils.enableElementAndLabel(myArtistDropWords, !databaseActive);
        myIgnoreArtworkInput.setEnabled(!databaseActive);
    }

    public class DeleteDatabaseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String optionOk = MyTunesRssUtils.getBundleString("ok");
            String optionCancel = MyTunesRssUtils.getBundleString("cancel");
            Object option = SwingUtils.showOptionsMessage(MyTunesRss.ROOT_FRAME, JOptionPane.QUESTION_MESSAGE, null, MyTunesRssUtils.getBundleString(
                    "question.deleteDatabase"), MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH, new Object[] {optionCancel, optionOk});
            if (optionOk.equals(option)) {
                MyTunesRssUtils.executeTask(null,
                                            MyTunesRssUtils.getBundleString("pleaseWait.recreatingDatabase"),
                                            null,
                                            false,
                                            new RecreateDatabaseTask());
            }
        }
    }

    public class UpdateDatabaseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MyTunesRssUtils.executeDatabaseUpdate();
        }
    }

    public class ChangeTriggerActionListener implements ItemListener {
        private int myListIndex;
        private int myTokenIndex;

        public ChangeTriggerActionListener(int listIndex, int tokenIndex) {
            myListIndex = listIndex;
            myTokenIndex = tokenIndex;
        }

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                List<String> triggers = MyTunesRss.CONFIG.getDatabaseCronTriggers();
                String[] tokens = triggers.remove(myListIndex).split(" ");
                tokens[myTokenIndex] = ((TriggerItem)e.getItem()).getKey();
                triggers.add(myListIndex, StringUtils.join(tokens, " "));
            }
        }
    }

    public class DeleteTriggerActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(myRootPanel,
                                                       MyTunesRssUtils.getBundleString("confirmation.deleteTrigger"),
                                                       MyTunesRssUtils.getBundleString("confirmation.titleDeleteTrigger"),
                                                       JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                MyTunesRss.CONFIG.getDatabaseCronTriggers().remove(Integer.parseInt(e.getActionCommand()));
            }
            refreshTriggers();
        }
    }

    public static class TriggerItem {
        private String myKey;
        private String myValue;

        public TriggerItem(String key, String value) {
            myKey = key;
            myValue = value;
        }

        public String getKey() {
            return myKey;
        }

        @Override
        public String toString() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof TriggerItem && myKey.equals(((TriggerItem)obj).myKey);
        }

        @Override
        public int hashCode() {
            return myKey.hashCode();
        }
    }
}