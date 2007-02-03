/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.network.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.swing.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;
import java.text.*;
import java.util.Date;

import com.intellij.uiDesigner.core.*;

/**
 * General settings panel
 */
public class General {
    private static final Log LOG = LogFactory.getLog(General.class);

    private static final int MAX_UPDATE_INTERVAL = 1440;
    private static final int MIN_UPDATE_INTERVAL = 1;

    private JPanel myRootPanel;
    private JTextField myPortInput;
    private JLabel myServerStatusLabel;
    private JButton myServerInfoButton;
    private JLabel myLastUpdatedLabel;
    private JCheckBox myIgnoreTimestampsInput;
    private JButton myDeleteDatabaseButton;
    private JCheckBox myAutoStartServerInput;
    private JCheckBox myUpdateOnStartInput;
    private JButton myProgramUpdateButton;
    private JCheckBox myAutoUpdateDatabaseInput;
    private JSpinner myAutoUpdateDatabaseIntervalInput;
    private JButton myUpdateDatabaseButton;
    private JCheckBox myUpdateDatabaseOnServerStart;
    private JPanel myServerPanel;
    private JTextField myServerNameInput;
    private JCheckBox myAvailableOnLocalNetInput;
    private boolean myUpdateOnStartInputCache;

    public void init() {
        myProgramUpdateButton.addActionListener(new ProgramUpdateButtonListener());
        myAutoStartServerInput.addActionListener(new AutoStartServerInputListener());
        myAutoUpdateDatabaseInput.addActionListener(new AutoUpdateDatabaseInputListener());
        myDeleteDatabaseButton.addActionListener(new DeleteDatabaseButtonListener());
        myUpdateDatabaseButton.addActionListener(new UpdateDatabaseButtonListener());
        myUpdateOnStartInput.setSelected(MyTunesRss.CONFIG.isCheckUpdateOnStart());
        myAutoStartServerInput.setSelected(MyTunesRss.CONFIG.isAutoStartServer());
        if (myAutoStartServerInput.isSelected()) {
            myUpdateOnStartInput.setSelected(false);
            myUpdateOnStartInput.setEnabled(false);
        }
        refreshLastUpdate();
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
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getPort()));
        myServerNameInput.setText(MyTunesRss.CONFIG.getServerName());
        myAvailableOnLocalNetInput.setSelected(MyTunesRss.CONFIG.isAvailableOnLocalNet());
        SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected());
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.idle"), null);
        myAvailableOnLocalNetInput.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (MyTunesRss.WEBSERVER.isRunning()) {
                    if (myAvailableOnLocalNetInput.isSelected()) {
                        MulticastService.startListener();
                    } else  {
                        MulticastService.stopListener();
                    }
                }
                SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected() && !MyTunesRss.WEBSERVER.isRunning());
            }
        });
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myPortInput, 1, 65535, false, MyTunesRss.BUNDLE.getString(
                "error.illegalServerPort")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myServerNameInput, MyTunesRss.BUNDLE.getString("error.emptyServerName")));
    }

    public Dimension getContentDimension() {
        Insets insets = ((AbstractLayout)myRootPanel.getLayout()).getMargin();
        return new Dimension(myRootPanel.getWidth() - insets.left - insets.right, myRootPanel.getHeight() - insets.top - insets.bottom);
    }

    public void refreshLastUpdate() {
        try {
            final SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (systemInformation.getLastUpdate() > 0) {
                        Date date = new Date(systemInformation.getLastUpdate());
                        myLastUpdatedLabel.setText(
                                MyTunesRss.BUNDLE.getString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRss.BUNDLE.getString(
                                        "settings.lastDatabaseUpdateDateFormat")).format(date));
                    } else {
                        myLastUpdatedLabel.setText(MyTunesRss.BUNDLE.getString("settings.databaseNotYetCreated"));
                    }
                    myRootPanel.validate();
                }
            });
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get last update time from database.", e);
            }

        }
    }

    public void setServerRunningStatus(int serverPort) {
        setServerStatus(MyTunesRss.BUNDLE.getString("serverStatus.running"), null);
        myRootPanel.validate();
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages != null) {
            return messages;
        } else {
            MyTunesRss.CONFIG.setPort(MyTunesRssUtils.getTextFieldInteger(myPortInput, -1));
            MyTunesRss.CONFIG.setCheckUpdateOnStart(myUpdateOnStartInput.isSelected());
            MyTunesRss.CONFIG.setAutoStartServer(myAutoStartServerInput.isSelected());
            MyTunesRss.CONFIG.setUpdateDatabaseOnServerStart(myUpdateDatabaseOnServerStart.isSelected());
            MyTunesRss.CONFIG.setAutoUpdateDatabase(myAutoUpdateDatabaseInput.isSelected());
            MyTunesRss.CONFIG.setAutoUpdateDatabaseInterval((Integer)myAutoUpdateDatabaseIntervalInput.getValue());
            MyTunesRss.CONFIG.setIgnoreTimestamps(myIgnoreTimestampsInput.isSelected());
            MyTunesRss.CONFIG.setServerName(myServerNameInput.getText());
            MyTunesRss.CONFIG.setAvailableOnLocalNet(myAvailableOnLocalNetInput.isSelected());
        }
        return null;
    }

    public void setGuiMode(GuiMode mode) {
        switch (mode) {
            case ServerRunning:
                SwingUtils.enableElementAndLabel(myPortInput, false);
                myAutoStartServerInput.setEnabled(false);
                myUpdateOnStartInput.setEnabled(false);
                myProgramUpdateButton.setEnabled(false);
                myAutoUpdateDatabaseInput.setEnabled(false);
                myUpdateDatabaseOnServerStart.setEnabled(false);
                myIgnoreTimestampsInput.setEnabled(false);
                myDeleteDatabaseButton.setEnabled(false);
                SwingUtils.enableElementAndLabel(myAutoUpdateDatabaseIntervalInput, false);
                SwingUtils.enableElementAndLabel(myServerNameInput, false);
                break;
            case ServerIdle:
                SwingUtils.enableElementAndLabel(myPortInput, true);
                myAutoStartServerInput.setEnabled(true);
                myUpdateOnStartInput.setEnabled(!myAutoStartServerInput.isSelected());
                myProgramUpdateButton.setEnabled(true);
                myUpdateDatabaseOnServerStart.setEnabled(true);
                myAutoUpdateDatabaseInput.setEnabled(true);
                myIgnoreTimestampsInput.setEnabled(true);
                myDeleteDatabaseButton.setEnabled(true);
                SwingUtils.enableElementAndLabel(myAutoUpdateDatabaseIntervalInput, myAutoUpdateDatabaseInput.isSelected());
                SwingUtils.enableElementAndLabel(myServerNameInput, myAvailableOnLocalNetInput.isSelected());
                break;
        }
    }

    public void setServerStatus(String text, String tooltipText) {
        if (text != null) {
            myServerStatusLabel.setText(text);
        }
        if (tooltipText != null) {
            myServerStatusLabel.setToolTipText(tooltipText);
        }
    }

    public class ProgramUpdateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            UpdateUtils.checkForUpdate(false);
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
            myRootPanel.validate();
        }
    }

    public class AutoUpdateDatabaseInputListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SwingUtils.enableElementAndLabel(myAutoUpdateDatabaseIntervalInput, myAutoUpdateDatabaseInput.isSelected());
            myRootPanel.validate();
        }
    }

    public class DeleteDatabaseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String optionOk = MyTunesRss.BUNDLE.getString("ok");
            String optionCancel = MyTunesRss.BUNDLE.getString("cancel");
            Object option = SwingUtils.showOptionsMessage(MyTunesRss.ROOT_FRAME, JOptionPane.QUESTION_MESSAGE, null, MyTunesRss.BUNDLE.getString(
                    "question.deleteDatabase"), MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH, new Object[] {optionCancel, optionOk});
            if (optionOk.equals(option)) {
                MyTunesRssUtils.executeTask(null,
                                            MyTunesRss.BUNDLE.getString("pleaseWait.recreatingDatabase"),
                                            null,
                                            false,
                                            new RecreateDatabaseTask());
            }
        }
    }

    public class UpdateDatabaseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            DatabaseBuilderTask task = MyTunesRss.createDatabaseBuilderTask();
            MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.buildDatabase"), null, false, task);
            if (!task.isExecuted()) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.updateNotRun"));
            }
        }
    }

    public class ServerInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new ServerInfo().display(MyTunesRss.ROOT_FRAME, myPortInput.getText());
        }
    }
}