package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.task.RecreateDatabaseTask;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private JButton myUpdateDatabaseButton;
    private JButton myDeleteDatabaseButton;
    private JPanel myConfigButtonsPanel;
    private Info myInfoForm;
    private SettingsForm[] mySettingsForms;

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

        int rows = 1;
        int rowWidth = 0;
        int lastPreferredWidth = 0;
        int limit = 400;
        myInfoForm = new Info();
        mySettingsForms = new SettingsForm[]{new Server(), new Database(), new DataSources(), new DataImport(), new Content(), new UserManagement(),
                new AdminNotify(), new Statistics(), new Misc(), new Streaming(), new Addons(), myInfoForm};
        for (SettingsForm form : mySettingsForms) {
            addSettingsItem(form);
            int itemWidth = myConfigButtonsPanel.getPreferredSize().width - lastPreferredWidth;
            rowWidth += itemWidth;
            if (rowWidth > limit) {
                rows++;
                rowWidth = itemWidth;
            }
            lastPreferredWidth = myConfigButtonsPanel.getPreferredSize().width;
        }
        Dimension d = myConfigButtonsPanel.getPreferredSize();
        myConfigButtonsPanel.setPreferredSize(new Dimension(400, d.height * rows));
        refreshLastUpdate();
        MyTunesRssEventManager.getInstance().addListener(this);
        initValues();
    }

    private void initValues() {
        setServerStatus(MyTunesRssUtils.getBundleString("serverStatus.idle"), null);
    }

    private void addSettingsItem(final SettingsForm settingsForm) {
        JButton button = new JButton(settingsForm.getDialogTitle());
        if (SystemUtils.IS_OS_MAC_OSX) {
            button.putClientProperty("JComponent.sizeVariant", "mini");
            button.putClientProperty("JButton.buttonType", "bevel");
        }
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSettings(settingsForm);
            }
        });
        myConfigButtonsPanel.add(button);
    }

    public String updateConfigFromGui() {
        StringBuffer messages = new StringBuffer();
        for (SettingsForm form : mySettingsForms) {
            String message = form.updateConfigFromGui();
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
        MyTunesRss.startWebserver();
    }

    public void doStopServer() {
        MyTunesRss.stopWebserver();
    }

    public void doQuitApplication() {
        MyTunesRssUtils.shutdownGracefully();
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

    protected void showSettings(final SettingsForm form) {
        form.initValues();
        String dialogTitle = MyTunesRssUtils.getBundleString("dialog.settings.commonTitle", form.getDialogTitle());
        final JDialog dialog = new JDialog(MyTunesRss.ROOT_FRAME, dialogTitle, true);
        dialog.getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(form.getClass());
                if (layout == null) {
                    layout = MyTunesRss.CONFIG.createDialogLayout(form.getClass());
                }
                layout.setX((int) dialog.getLocation().getX());
                layout.setY((int) dialog.getLocation().getY());
                layout.setWidth((int) dialog.getSize().getWidth());
                layout.setHeight((int) dialog.getSize().getHeight());
                String messages = form.updateConfigFromGui();
                if (messages != null) {
                    MyTunesRssUtils.showErrorMessage(messages);
                } else {
                    dialog.dispose();
                }
            }
        });
        dialog.add(form.getRootPanel());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(form.getClass());
        dialog.pack();
        final Dimension minimalDimension = dialog.getSize();
        dialog.setMinimumSize(minimalDimension);
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                Dimension d = e.getComponent().getSize();
                boolean changed = false;
                if (d.width < minimalDimension.width) {
                    d.width = minimalDimension.width;
                    changed = true;
                }
                if (d.height < minimalDimension.height) {
                    d.height = minimalDimension.height;
                    changed = true;
                }
                if (changed) {
                    e.getComponent().setSize(d);
                }
            }
        });
        if (layout != null && layout.isValid()) {
            dialog.setLocation(layout.getX(), layout.getY());
            dialog.setSize(layout.getWidth(), layout.getHeight());
            dialog.setVisible(true);
        } else {
            SwingUtils.packAndShowRelativeTo(dialog, MyTunesRss.ROOT_FRAME);
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
                    "question.deleteDatabase"), MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH, new Object[]{optionCancel, optionOk});
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