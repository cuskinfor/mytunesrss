package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.task.RecreateDatabaseTask;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.sql.DataStoreSession;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.settings.Settings
 */
public class Settings implements MyTunesRssEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private JPanel myRootPanel;
    private JButton myStartServerButton;
    private JButton myStopServerButton;
    private JButton myQuitButton;
    private JLabel myServerStatusLabel;
    private JButton myServerInfoButton;
    private JLabel myLastUpdatedLabel;
    private JComboBox mySettingsInput;
    private JButton myUpdateDatabaseButton;
    private JButton myDeleteDatabaseButton;
    private Info myInfoForm;

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void init() {
        myStartServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartServer();
            }
        });
        myStopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStopServer();
            }
        });
        myStopServerButton.setEnabled(false);
        myQuitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuitApplication();
            }
        });
        myServerInfoButton.addActionListener(new ServerInfoButtonListener());
        myDeleteDatabaseButton.addActionListener(new DeleteDatabaseButtonListener());
        myUpdateDatabaseButton.addActionListener(new UpdateDatabaseButtonListener());

        mySettingsInput.addItem("Configuration settings"); // todo i18n
        addSettingsItem(new Server());
        addSettingsItem(new Database());
        addSettingsItem(new Directories());
        addSettingsItem(new Content());
        addSettingsItem(new UserManagement());
        addSettingsItem(new Misc());
        addSettingsItem(new Streaming());
        addSettingsItem(new Addons());
        myInfoForm = new Info();
        addSettingsItem(myInfoForm);
        mySettingsInput.addActionListener(new SelectSettingsListener());

        refreshLastUpdate();
        MyTunesRssEventManager.getInstance().addListener(this);
        initValues();
    }

    private void initValues() {
        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
    }

    private void addSettingsItem(SettingsForm settingsForm) {
        settingsForm.init();
        mySettingsInput.addItem(settingsForm);
    }

    public String updateConfigFromGui() {
        StringBuffer messages = new StringBuffer();
        for (int i = 1; i < mySettingsInput.getItemCount(); i++) {
            String message = ((SettingsForm)mySettingsInput.getItemAt(i)).updateConfigFromGui();
            if (message != null) {
                messages.append(message).append(" ");
            }
        }
        String returnValue = messages.toString().trim();
        return returnValue.length() > 0 ? returnValue : null;
    }

    public void handleEvent(MyTunesRssEvent event) {
        switch (event) {
            case SERVER_STARTED:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerRunning();
                }
                myStartServerButton.setEnabled(false);
                myStopServerButton.setEnabled(true);
                setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.running"), null);
                myRootPanel.validate();
                break;
            case SERVER_STOPPED:
                if (MyTunesRss.SYSTRAYMENU != null) {
                    MyTunesRss.SYSTRAYMENU.setServerStopped();
                }
                myStartServerButton.setEnabled(true);
                myStopServerButton.setEnabled(false);
                setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
                myRootPanel.validate();
                break;
            case DATABASE_UPDATE_STATE_CHANGED:
                myUpdateDatabaseButton.setEnabled(false);
                myDeleteDatabaseButton.setEnabled(false);
                myLastUpdatedLabel.setText(MyTunesRssUtils.getBundleString(event.getMessageKey(), event.getMessageParams()));
                break;
            case DATABASE_UPDATE_FINISHED:
                myUpdateDatabaseButton.setEnabled(true);
                myDeleteDatabaseButton.setEnabled(true);
                refreshLastUpdate();
                break;
        }
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
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get last update time from database.", e);
            }

        } finally {
            session.commit();
        }
    }

    public void doStartServer() {
        String messages = updateConfigFromGui();
        if (messages == null) {
            MyTunesRss.startWebserver();
        } else {
            MyTunesRssUtils.showErrorMessage(messages);
        }
    }

    public void doStopServer() {
        MyTunesRss.stopWebserver();
    }

    public void doQuitApplication() {
        String messages = updateConfigFromGui();
        if (messages == null) {
            MyTunesRssUtils.shutdownGracefully();
        } else {
            MyTunesRssUtils.showErrorMessage(messages);
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

    public void forceRegistration() {
        myInfoForm.forceRegistration();
    }

    public class SelectSettingsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final SettingsForm form = (SettingsForm)mySettingsInput.getSelectedItem();
            final JDialog dialog = new JDialog(MyTunesRss.ROOT_FRAME, form.toString(), true); // todo correct dialog title, maybe common layout framing the form
            DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(form.getClass());
            if (layout != null) {
                dialog.setLocation(layout.getX(), layout.getY());
                dialog.setSize(layout.getWidth(), layout.getHeight());
            }
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(form.getClass());
                    if (layout == null) {
                        layout = MyTunesRss.CONFIG.createDialogLayout(form.getClass());
                    }
                    layout.setX((int)dialog.getLocation().getX());
                    layout.setY((int)dialog.getLocation().getY());
                    layout.setWidth((int)dialog.getSize().getWidth());
                    layout.setHeight((int)dialog.getSize().getHeight());
                    form.updateConfigFromGui();
                }
            });
            dialog.add(form.getRootPanel());
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            if (layout != null && layout.isValid()) {
                dialog.setVisible(true);
            } else {
                SwingUtils.packAndShowRelativeTo(dialog, MyTunesRss.ROOT_FRAME);
            }
            mySettingsInput.setSelectedIndex(0);
        }
    }

    public class ServerInfoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            new ServerInfo().display(MyTunesRss.ROOT_FRAME, Integer.toString(MyTunesRss.CONFIG.getPort()));
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