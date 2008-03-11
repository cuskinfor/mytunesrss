/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.mytunesrss.task.RecreateDatabaseTask;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
                MyTunesRss.CONFIG.getDatabaseCronTriggers().add("0 0 0 ? * SUN-SAT");
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
        JComboBox comboBox = new JComboBox(MyTunesRssJobUtils.getDays());
        comboBox.setSelectedItem(new MyTunesRssJobUtils.TriggerItem(triggerParts[5], MyTunesRssUtils.getBundleString(
                "settings.cron.day." + triggerParts[5])));
        comboBox.addItemListener(new ChangeTriggerActionListener(row, 5));
        addPanelComponent(comboBox, createConstraints(row, 0));
        comboBox = new JComboBox(MyTunesRssJobUtils.getHours());
        comboBox.setSelectedItem(new MyTunesRssJobUtils.TriggerItem(triggerParts[2], triggerParts[2]));
        comboBox.addItemListener(new ChangeTriggerActionListener(row, 2));
        addPanelComponent(comboBox, createConstraints(row, 1));
        comboBox = new JComboBox(MyTunesRssJobUtils.getMinutes());
        comboBox.setSelectedItem(new MyTunesRssJobUtils.TriggerItem(triggerParts[1], triggerParts[1]));
        comboBox.addItemListener(new ChangeTriggerActionListener(row, 1));
        addPanelComponent(comboBox, createConstraints(row, 2));
        JButton delete = new JButton(MyTunesRssUtils.getBundleString("settings.deleteTrigger"));
        delete.setToolTipText(MyTunesRssUtils.getBundleString("settings.deleteTriggerTooltip"));
        delete.setActionCommand(Integer.toString(row));
        delete.setOpaque(false);
        delete.addActionListener(myDeleteTriggerActionListener);
        addPanelComponent(delete, createConstraints(row, 3));
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
                tokens[myTokenIndex] = ((MyTunesRssJobUtils.TriggerItem)e.getItem()).getKey();
                triggers.add(myListIndex, StringUtils.join(tokens, " "));
                MyTunesRssJobUtils.scheduleDatabaseJob();
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
            MyTunesRssJobUtils.scheduleDatabaseJob();
            refreshTriggers();
        }
    }
}