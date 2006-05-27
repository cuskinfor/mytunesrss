/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.settings.Options
 */
public class Options {
    private static final Log LOG = LogFactory.getLog(Options.class);

    private JPanel myRootPanel;
    private JSpinner myMaxMemInput;
    private JLabel myMaxMemLabel;
    private JButton myMaxMemSaveButton;
    private JCheckBox myAutoStartServerInput;
    private JCheckBox myUpdateOnStartInput;
    private boolean myUpdateOnStartInputCache;
    private JButton myProgramUpdateButton;
    private JButton myDatabaseUpdateButton;
    private JButton myDatabaseRefreshButton;
    private JButton myDatabaseRecreateButton;
    private JLabel myLastUpdatedLabel;
    private Settings mySettingsForm;

    public void init(Settings settingsForm) {
        mySettingsForm = settingsForm;
        refreshLastUpdate();
        int minMemory = ProgramUtils.getMemorySwitch(MemorySwitchType.Minimum);
        int maxMemory = ProgramUtils.getMemorySwitch(MemorySwitchType.Maxmimum);
        if (maxMemory != -1) {
            SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(maxMemory, Math.max(10, minMemory), 500, 10);
            myMaxMemInput.setModel(spinnerNumberModel);
        } else {
            myMaxMemLabel.setVisible(false);
            myMaxMemInput.setVisible(false);
            myMaxMemSaveButton.setVisible(false);
            myMaxMemSaveButton.addActionListener(new MaxMemSaveButtonListener());
        }
        myUpdateOnStartInput.setSelected(MyTunesRss.CONFIG.isCheckUpdateOnStart());
        myAutoStartServerInput.setSelected(MyTunesRss.CONFIG.isAutoStartServer());
        if (myAutoStartServerInput.isSelected()) {
            myUpdateOnStartInput.setSelected(false);
            myUpdateOnStartInput.setEnabled(false);
        }
        myProgramUpdateButton.addActionListener(new ProgramUpdateButtonListener());
        myDatabaseUpdateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runBuildDatabaseTask(DatabaseBuilderTask.BuildType.Update);
            }
        });
        myDatabaseRefreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runBuildDatabaseTask(DatabaseBuilderTask.BuildType.Refresh);
            }
        });
        myDatabaseRecreateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runBuildDatabaseTask(DatabaseBuilderTask.BuildType.Recreate);
            }
        });
        myAutoStartServerInput.addActionListener(new AutoStartServerInputListener());
    }

    private void refreshLastUpdate() {
        List<Long> result = null;
        try {
            result = (List<Long>)MyTunesRss.STORE.executeQuery(new DataStoreQuery<Long>() {
                public Collection<Long> execute(Connection connection) throws SQLException {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT lastupdate AS lastupdate FROM mytunesrss");
                    if (resultSet.next()) {
                        return Collections.singletonList(Long.valueOf(resultSet.getLong("LASTUPDATE")));
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get last update time from database.", e);
            }

        }
        if (result != null && !result.isEmpty()) {
            Date date = new Date(result.get(0).longValue());
            myLastUpdatedLabel.setText("Last updated: " + new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(date));
            myDatabaseRefreshButton.setEnabled(true);
            myDatabaseUpdateButton.setEnabled(true);
        } else {
            myLastUpdatedLabel.setText("Database has never been created.");
            myDatabaseRefreshButton.setEnabled(false);
            myDatabaseUpdateButton.setEnabled(false);
        }
        myRootPanel.validate();
    }

    private void runBuildDatabaseTask(DatabaseBuilderTask.BuildType buildType) {
        try {
            PleaseWait.start(mySettingsForm.getFrame(),
                             null,
                             buildType.getVerb() + " database... please wait.",
                             false,
                             false,
                             new DatabaseBuilderTask(new File(mySettingsForm.getGeneralForm().getTunesXmlPathInput().getText()).toURL(), buildType));
            refreshLastUpdate();
        } catch (MalformedURLException e1) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not build database.", e1);
            }

        }
    }

    public void updateConfigFromGui() {
        MyTunesRss.CONFIG.setCheckUpdateOnStart(myUpdateOnStartInput.isSelected());
        MyTunesRss.CONFIG.setAutoStartServer(myAutoStartServerInput.isSelected());
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myMaxMemInput, false);
                myMaxMemSaveButton.setEnabled(false);
                myAutoStartServerInput.setEnabled(false);
                myUpdateOnStartInput.setEnabled(false);
                myProgramUpdateButton.setEnabled(false);
                myDatabaseRefreshButton.setEnabled(false);
                myDatabaseRecreateButton.setEnabled(false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myMaxMemInput, true);
                myMaxMemSaveButton.setEnabled(true);
                myAutoStartServerInput.setEnabled(true);
                myUpdateOnStartInput.setEnabled(true);
                myProgramUpdateButton.setEnabled(true);
                myDatabaseRefreshButton.setEnabled(true);
                myDatabaseRecreateButton.setEnabled(true);
                break;
        }
    }

    public class MaxMemSaveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int maxMem = ((Integer)myMaxMemInput.getValue()).intValue();
            if (ProgramUtils.updateMemorySwitch(MemorySwitchType.Maxmimum, maxMem)) {
                SwingUtils.showInfoMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("info.savemem.success"));
            } else {
                SwingUtils.showErrorMessage(mySettingsForm.getFrame(), MyTunesRss.BUNDLE.getString("error.memsave.failure"));
            }
        }
    }

    public class ProgramUpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new Updater(mySettingsForm.getFrame()).checkForUpdate(false);
                }
            });
        }
    }

    public class AutoStartServerInputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (myAutoStartServerInput.isSelected()) {
                myUpdateOnStartInputCache = myUpdateOnStartInput.isSelected();
                myUpdateOnStartInput.setSelected(false);
                myUpdateOnStartInput.setEnabled(false);
            } else {
                myUpdateOnStartInput.setSelected(myUpdateOnStartInputCache);
                myUpdateOnStartInput.setEnabled(true);
            }
        }
    }
}