/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.swing.JTextFieldValidation;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.Database
 */
public class Database implements MyTunesRssEventListener, SettingsForm {
    private JPanel myRootPanel;
    private JCheckBox myUpdateDatabaseOnServerStart;
    private JCheckBox myDeleteMissingFiles;
    private JScrollPane myScrollPane;
    private JPanel mySchedulePanel;
    private JComboBox myDbTypeInput;
    private JTextField myDbDriverInput;
    private JTextField myDbConnectInput;
    private JTextField myDbUserInput;
    private JPasswordField myDbPassInput;
    private JPanel myDbExtraPanel;
    private DeleteTriggerActionListener myDeleteTriggerActionListener = new DeleteTriggerActionListener();

    public Database() {
        myScrollPane.getViewport().setOpaque(false);
        refreshTriggers();
        myDbExtraPanel.setVisible(!DatabaseType.h2.name().equals(MyTunesRss.CONFIG.getDatabaseType()));
        myDbTypeInput.addItem(DatabaseType.h2);
        myDbTypeInput.addItem(DatabaseType.h2custom);
        myDbTypeInput.addItem(DatabaseType.postgres);
        myDbTypeInput.addItem(DatabaseType.mysql);
        myDbTypeInput.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (myRootPanel.isShowing() && e.getStateChange() == ItemEvent.DESELECTED && e.getItem() == DatabaseType.h2) {
                    MyTunesRssUtils.showInfoMessage(MyTunesRssUtils.getBundleString("info.selectOtherDatabaseType"));
                }
                myDbExtraPanel.setVisible(myDbTypeInput.getSelectedItem() != DatabaseType.h2);
            }
        });
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
        myUpdateDatabaseOnServerStart.setSelected(MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart());
        myDeleteMissingFiles.setSelected(MyTunesRss.CONFIG.isItunesDeleteMissingFiles());
        myDbTypeInput.setSelectedItem(DatabaseType.valueOf(MyTunesRss.CONFIG.getDatabaseType()));
        myDbDriverInput.setText(MyTunesRss.CONFIG.getDatabaseDriver());
        myDbConnectInput.setText(MyTunesRss.CONFIG.getDatabaseConnection());
        myDbUserInput.setText(MyTunesRss.CONFIG.getDatabaseUser());
        myDbPassInput.setText(MyTunesRss.CONFIG.getDatabasePassword());
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setUpdateDatabaseOnServerStart(myUpdateDatabaseOnServerStart.isSelected());
            MyTunesRss.CONFIG.setItunesDeleteMissingFiles(myDeleteMissingFiles.isSelected());
            if (databaseChanged()) {
                MyTunesRssUtils.showInfoMessage(MyTunesRssUtils.getBundleString("settings.databaseChangedWarning"));
            }
            DatabaseType databaseType = (DatabaseType) myDbTypeInput.getSelectedItem();
            MyTunesRss.CONFIG.setDatabaseType(databaseType != null ? databaseType.name() : DatabaseType.h2.name());
            MyTunesRss.CONFIG.setDatabaseDriver(myDbDriverInput.getText());
            MyTunesRss.CONFIG.setDatabaseConnection(myDbConnectInput.getText());
            MyTunesRss.CONFIG.setDatabaseUser(myDbUserInput.getText());
            MyTunesRss.CONFIG.setDatabasePassword(new String(myDbPassInput.getPassword()));
        }
        return null;
    }

    private boolean databaseChanged() {
        DatabaseType databaseType = (DatabaseType) myDbTypeInput.getSelectedItem();
        String newType = StringUtils.trimToEmpty(databaseType != null ? databaseType.name() : DatabaseType.h2.name());
        String newDriver = StringUtils.trimToEmpty(myDbDriverInput.getText());
        String newConnect = StringUtils.trimToEmpty(myDbConnectInput.getText());
        String newUser = StringUtils.trimToEmpty(myDbUserInput.getText());
        String newPass = StringUtils.trimToEmpty(new String(myDbPassInput.getPassword()));
        if (!newType.equals(MyTunesRss.CONFIG.getDatabaseType())) {
            return true;
        }
        if (!newDriver.equals(MyTunesRss.CONFIG.getDatabaseDriver())) {
            return true;
        }
        if (!newConnect.equals(MyTunesRss.CONFIG.getDatabaseConnection())) {
            return true;
        }
        if (!newUser.equals(MyTunesRss.CONFIG.getDatabaseUser())) {
            return true;
        }
        if (!newPass.equals(MyTunesRss.CONFIG.getDatabasePassword())) {
            return true;
        }
        return false;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void setGuiMode(GuiMode mode) {
        boolean serverActive = MyTunesRss.WEBSERVER.isRunning() || mode == GuiMode.ServerRunning;
        boolean databaseActive = DatabaseBuilderTask.isRunning() || mode == GuiMode.DatabaseUpdating;
        myDbTypeInput.setEnabled(!databaseActive && !serverActive);
        myDbDriverInput.setEnabled(!databaseActive && !serverActive);
        myDbConnectInput.setEnabled(!databaseActive && !serverActive);
        myDbUserInput.setEnabled(!databaseActive && !serverActive);
        myDbPassInput.setEnabled(!databaseActive && !serverActive);
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.database.title");
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

    public enum DatabaseType {
        h2(), h2custom(), postgres(), mysql();


        @Override
        public String toString() {
            return MyTunesRssUtils.getBundleString("settings.database.type." + name());
        }
    }
}