/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.server.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.settings.Database
 */
public class Database implements MyTunesRssEventListener {
    private static final Log LOG = LogFactory.getLog(Database.class);
    private static final int MAX_UPDATE_INTERVAL = 1440;
    private static final int MIN_UPDATE_INTERVAL = 1;

    private JPanel myRootPanel;
    private JLabel myLastUpdatedLabel;
    private JCheckBox myIgnoreTimestampsInput;
    private JButton myDeleteDatabaseButton;
    private JButton myUpdateDatabaseButton;
    private JTextField myArtistDropWords;
    private JTextField myFileTypes;
    private JCheckBox myUpdateDatabaseOnServerStart;
    private JCheckBox myAutoUpdateDatabaseInput;
    private JSpinner myAutoUpdateDatabaseIntervalInput;
    private JCheckBox myDeleteMissingFiles;
    private JCheckBox myIgnoreArtworkInput;

    public void init() {
        refreshLastUpdate();
        myAutoUpdateDatabaseInput.addActionListener(new AutoUpdateDatabaseInputListener());
        myDeleteDatabaseButton.addActionListener(new DeleteDatabaseButtonListener());
        myUpdateDatabaseButton.addActionListener(new UpdateDatabaseButtonListener());
        initValues();
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void handleEvent(MyTunesRssEvent event) {
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

    private void initValues() {
        int interval = MyTunesRss.CONFIG.getAutoUpdateDatabaseInterval();
        if (interval < MIN_UPDATE_INTERVAL) {
            interval = MIN_UPDATE_INTERVAL;
        } else if (interval > MAX_UPDATE_INTERVAL) {
            interval = MAX_UPDATE_INTERVAL;
        }
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(interval, MIN_UPDATE_INTERVAL, MAX_UPDATE_INTERVAL, 1);
        myAutoUpdateDatabaseIntervalInput.setModel(spinnerNumberModel);
        myAutoUpdateDatabaseInput.setSelected(MyTunesRss.CONFIG.isAutoUpdateDatabase());
        myUpdateDatabaseOnServerStart.setSelected(MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart());
        myIgnoreTimestampsInput.setSelected(MyTunesRss.CONFIG.isIgnoreTimestamps());
        SwingUtils.enableElementAndLabel(myAutoUpdateDatabaseIntervalInput, MyTunesRss.CONFIG.isAutoUpdateDatabase());
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
                        myLastUpdatedLabel.setText(
                                MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRssUtils.getBundleString(
                                        "settings.lastDatabaseUpdateDateFormat")).format(date));
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
            MyTunesRss.CONFIG.setAutoUpdateDatabase(myAutoUpdateDatabaseInput.isSelected());
            MyTunesRss.CONFIG.setAutoUpdateDatabaseInterval((Integer)myAutoUpdateDatabaseIntervalInput.getValue());
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
        myAutoUpdateDatabaseInput.setEnabled(!serverActive);
        myUpdateDatabaseOnServerStart.setEnabled(!serverActive);
        myIgnoreTimestampsInput.setEnabled(!databaseActive);
        myDeleteDatabaseButton.setEnabled(!databaseActive);
        SwingUtils.enableElementAndLabel(myAutoUpdateDatabaseIntervalInput, myAutoUpdateDatabaseInput.isSelected() && !serverActive);
        myDeleteMissingFiles.setEnabled(!databaseActive);
        SwingUtils.enableElementAndLabel(myFileTypes, !databaseActive);
        SwingUtils.enableElementAndLabel(myArtistDropWords, !databaseActive);
        myIgnoreArtworkInput.setEnabled(!databaseActive);
    }

    public class AutoUpdateDatabaseInputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SwingUtils.enableElementAndLabel(myAutoUpdateDatabaseIntervalInput, myAutoUpdateDatabaseInput.isSelected());
            myRootPanel.validate();
        }
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
}