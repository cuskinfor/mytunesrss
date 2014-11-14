/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.DatabaseJobRunningException;
import de.codewave.mytunesrss.FetchExternalAddressRunnable;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.datastore.OrphanedImageRemover;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.RecreateHelpTablesStatement;
import de.codewave.mytunesrss.datastore.statement.RemoveImagesForDataSourcesStatement;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventListener;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.mytunesrss.webadmin.datasource.DatasourcesConfigPanel;
import de.codewave.mytunesrss.webadmin.datasource.DatasourcesSelectionPanel;
import de.codewave.utils.network.NetworkUtils;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.SinglePanelWindow;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatusPanel extends Panel implements Button.ClickListener, MyTunesRssEventListener, Refresher.RefreshListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusPanel.class);

    private Panel myAlertPanel;
    private Label myServerStatus;
    private Label myDatabaseStatus;
    private Button myStartServer;
    private Button myStopServer;
    private Table myInternalAddresses;
    private Table myExternalAddresses;
    private Table myConnections;
    private Button myUpdateDatabase;
    private Button myRemoveImages;
    private Button myStopDatabaseUpdate;
    private Button myResetDatabase;
    private Button myBackupDatabase;
    private Button myDatabaseMaintenance;
    private Button myServerConfig;
    private Button myDatabaseConfig;
    private Button myDatasourcesConfig;
    private Button myContentConfig;
    private Button myUsersConfig;
    private Button myNotificationsConfig;
    private Button myStatisticsConfig;
    private Button myMiscConfig;
    private Button myStreamingConfig;
    private Button myUpnpServerConfig;
    private Button myAddonsConfig;
    private Button mySupportConfig;
    private Button myHelp;
    private Button myLogout;
    private Refresher myRefresher;
    private Button myQuitMyTunesRss;

    public void attach() {
        super.attach();
        MyTunesRssEventManager.getInstance().addListener(this);
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        setCaption(getApplication().getBundleString("statusPanel.caption"));
        Embedded logo = new Embedded("", new ClassResource("/de/codewave/mytunesrss/mytunesrss.png", getApplication()));
        logo.setWidth(290, Sizeable.UNITS_PIXELS);
        logo.setHeight(88, Sizeable.UNITS_PIXELS);
        addComponent(logo);
        myAlertPanel = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        myAlertPanel.addStyleName("alertPanel");
        myAlertPanel.setVisible(false);
        addComponent(myAlertPanel);
        Panel server = new Panel(getApplication().getBundleString("statusPanel.server.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(server);
        ((Layout) server.getContent()).setMargin(true);
        ((Layout.SpacingHandler) server.getContent()).setSpacing(true);
        Panel serverAccordionPanel = new Panel();
        Accordion accordion = new Accordion();
        serverAccordionPanel.setContent(accordion);
        server.addComponent(serverAccordionPanel);
        Panel serverGeneral = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        serverGeneral.addStyleName("light");
        accordion.addTab(serverGeneral, getApplication().getBundleString("statusPanel.serverGeneral.caption"), null);
        myServerStatus = new Label();
        myServerStatus.setWidth("100%");
        myServerStatus.addStyleName("statusmessage");
        serverGeneral.addComponent(myServerStatus);
        Panel serverButtons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        serverButtons.addStyleName("light");
        serverGeneral.addComponent(serverButtons);
        myStartServer = getApplication().getComponentFactory().createButton("statusPanel.server.start", StatusPanel.this);
        myStopServer = getApplication().getComponentFactory().createButton("statusPanel.server.stop", StatusPanel.this);
        serverButtons.addComponent(myStartServer);
        serverButtons.addComponent(myStopServer);
        Panel serverAddresses = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        serverAddresses.addStyleName("light");
        myInternalAddresses = new Table();
        myInternalAddresses.setCacheRate(50);
        myInternalAddresses.addContainerProperty("address", String.class, null, getApplication().getBundleString("statusPanel.internalServerAddresses"), null, null);
        myInternalAddresses.setPageLength(0);
        myExternalAddresses = new Table();
        myExternalAddresses.setCacheRate(50);
        myExternalAddresses.addContainerProperty("address", String.class, null, getApplication().getBundleString("statusPanel.externalServerAddress"), null, null);
        myExternalAddresses.setPageLength(0);
        serverAddresses.addComponent(myInternalAddresses);
        serverAddresses.addComponent(myExternalAddresses);
        accordion.addTab(serverAddresses, getApplication().getBundleString("statusPanel.serverAddresses.caption"), null);
        Panel serverConnections = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        serverConnections.addStyleName("light");
        myConnections = new Table();
        myConnections.setCacheRate(50);
        myConnections.addContainerProperty("remoteAddress", String.class, null, getApplication().getBundleString("statusPanel.connectionRemoteAddress"), null, null);
        myConnections.addContainerProperty("user", String.class, null, getApplication().getBundleString("statusPanel.connectionUser"), null, null);
        myConnections.addContainerProperty("sessions", Long.class, null, getApplication().getBundleString("statusPanel.connectionSessions"), null, null);
        myConnections.addContainerProperty("accessTime", Date.class, null, getApplication().getBundleString("statusPanel.connectionAccessTime"), null, null);
        myConnections.addContainerProperty("bytesStreamed", String.class, null, getApplication().getBundleString("statusPanel.connectionDownBytes"), null, null);
        myConnections.setPageLength(0);
        serverConnections.addComponent(myConnections);
        accordion.addTab(serverConnections, getApplication().getBundleString("statusPanel.serverConnections.caption"), null);
        Panel database = new Panel(getApplication().getBundleString("statusPanel.database.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(database);
        myDatabaseStatus = new Label();
        myDatabaseStatus.setWidth("100%");
        myDatabaseStatus.addStyleName("statusmessage");
        database.addComponent(myDatabaseStatus);
        Panel databaseButtons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        databaseButtons.addStyleName("light");
        database.addComponent(databaseButtons);
        myUpdateDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.update", StatusPanel.this);
        myRemoveImages = getApplication().getComponentFactory().createButton("statusPanel.database.removeImages", StatusPanel.this);
        myStopDatabaseUpdate = getApplication().getComponentFactory().createButton("statusPanel.database.stopUpdate", StatusPanel.this);
        myResetDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.reset", StatusPanel.this);
        myBackupDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.backup", StatusPanel.this);
        myDatabaseMaintenance = getApplication().getComponentFactory().createButton("statusPanel.database.maintenance", StatusPanel.this);
        databaseButtons.addComponent(myUpdateDatabase);
        databaseButtons.addComponent(myRemoveImages);
        databaseButtons.addComponent(myStopDatabaseUpdate);
        databaseButtons.addComponent(myResetDatabase);
        databaseButtons.addComponent(myBackupDatabase);
        databaseButtons.addComponent(myDatabaseMaintenance);
        Panel configButtons = new Panel(getApplication().getBundleString("statusPanel.config.caption"), getApplication().getComponentFactory().createGridLayout(4, 3, true, true));
        addComponent(configButtons);
        myServerConfig = getApplication().getComponentFactory().createButton("statusPanel.config.server", StatusPanel.this);
        myDatabaseConfig = getApplication().getComponentFactory().createButton("statusPanel.config.database", StatusPanel.this);
        myDatasourcesConfig = getApplication().getComponentFactory().createButton("statusPanel.config.datasources", StatusPanel.this);
        myContentConfig = getApplication().getComponentFactory().createButton("statusPanel.config.contents", StatusPanel.this);
        myUsersConfig = getApplication().getComponentFactory().createButton("statusPanel.config.users", StatusPanel.this);
        myNotificationsConfig = getApplication().getComponentFactory().createButton("statusPanel.config.notifications", StatusPanel.this);
        myStatisticsConfig = getApplication().getComponentFactory().createButton("statusPanel.config.statistics", StatusPanel.this);
        myMiscConfig = getApplication().getComponentFactory().createButton("statusPanel.config.misc", StatusPanel.this);
        myStreamingConfig = getApplication().getComponentFactory().createButton("statusPanel.config.streaming", StatusPanel.this);
        myUpnpServerConfig = getApplication().getComponentFactory().createButton("statusPanel.config.upnpserver", StatusPanel.this);
        myAddonsConfig = getApplication().getComponentFactory().createButton("statusPanel.config.addons", StatusPanel.this);
        mySupportConfig = getApplication().getComponentFactory().createButton("statusPanel.config.support", StatusPanel.this);
        configButtons.addComponent(myServerConfig);
        configButtons.addComponent(myDatabaseConfig);
        configButtons.addComponent(myDatasourcesConfig);
        configButtons.addComponent(myContentConfig);
        configButtons.addComponent(myUsersConfig);
        configButtons.addComponent(myNotificationsConfig);
        configButtons.addComponent(myStatisticsConfig);
        configButtons.addComponent(myMiscConfig);
        configButtons.addComponent(myStreamingConfig);
        configButtons.addComponent(myUpnpServerConfig);
        configButtons.addComponent(myAddonsConfig);
        configButtons.addComponent(mySupportConfig);
        Panel buttons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        buttons.addStyleName("light");
        addComponent(buttons);
        myHelp = getApplication().getComponentFactory().createButton("statusPanel.help", StatusPanel.this);
        myLogout = getApplication().getComponentFactory().createButton("statusPanel.logout", StatusPanel.this);
        myQuitMyTunesRss = getApplication().getComponentFactory().createButton("statusPanel.quitMyTunesRss", StatusPanel.this);
        buttons.addComponent(myHelp);
        if (MyTunesRss.CONFIG.isAdminPassword()) {
            buttons.addComponent(myLogout);
        }
        buttons.addComponent(myQuitMyTunesRss);
        myServerStatus.setValue(MyTunesRss.WEBSERVER.isRunning() ? getApplication().getBundleString("statusPanel.serverRunning") : getApplication().getBundleString("statusPanel.serverStopped"));
        MyTunesRssEvent event = MyTunesRss.LAST_DATABASE_EVENT.get();
        myDatabaseStatus.setValue(MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() ? MyTunesRssUtils.getBundleString(getLocale(), event.getMessageKey(), event.getMessageParams()) : getLastDatabaseUpdateText());
        myRefresher = new Refresher();
        addComponent(myRefresher);
        myRefresher.setRefreshInterval(MyTunesRssWebAdmin.ADMIN_REFRESHER_INTERVAL_MILLIS);
        myRefresher.addListener(this);
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());
        myUpdateDatabase.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseResetRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseBackupRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
        myRemoveImages.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseResetRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseBackupRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
        myStopDatabaseUpdate.setEnabled(MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseResetRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseBackupRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
        myResetDatabase.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseResetRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseBackupRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
        myBackupDatabase.setEnabled(MyTunesRss.CONFIG.isDefaultDatabase() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseResetRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseBackupRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
        myDatabaseMaintenance.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseResetRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseBackupRunning() && !MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
        refreshAlert();
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).checkUnhandledException();
    }

    @Override
    public void detach() {
        super.detach();
        MyTunesRssEventManager.getInstance().removeListener(this);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myLogout) {
            getApplication().close();
        } else if (clickEvent.getButton() == myServerConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new ServerConfigPanel());
        } else if (clickEvent.getButton() == myDatabaseConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new DatabaseConfigPanel());
        } else if (clickEvent.getButton() == myDatasourcesConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new DatasourcesConfigPanel());
        } else if (clickEvent.getButton() == myContentConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new ContentConfigPanel());
        } else if (clickEvent.getButton() == myUsersConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new UserConfigPanel());
        } else if (clickEvent.getButton() == myNotificationsConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new AdminNotificationsConfigPanel());
        } else if (clickEvent.getButton() == myStatisticsConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new StatisticsConfigPanel());
        } else if (clickEvent.getButton() == myMiscConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new MiscConfigPanel());
        } else if (clickEvent.getButton() == myStreamingConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new StreamingConfigPanel());
        } else if (clickEvent.getButton() == myUpnpServerConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new UpnpServerConfigPanel());
        } else if (clickEvent.getButton() == myAddonsConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new AddonsConfigPanel());
        } else if (clickEvent.getButton() == mySupportConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new SupportConfigPanel());
        } else if (clickEvent.getSource() == myStartServer) {
            myStartServer.setEnabled(false);
            MyTunesRss.startWebserver();
        } else if (clickEvent.getSource() == myStopServer) {
            myStopServer.setEnabled(false);
            MyTunesRss.stopWebserver();
        } else if (clickEvent.getSource() == myUpdateDatabase) {
            new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getApplication().getBundleString("datasourceSelection.caption"), new DatasourcesSelectionPanel(true) {
                @Override
                protected void onContinue(final Collection<DatasourceConfig> datasources, final boolean ignoreTimestamps) {
                    myUpdateDatabase.setEnabled(false);
                    myRemoveImages.setEnabled(false);
                    myStopDatabaseUpdate.setEnabled(true);
                    myResetDatabase.setEnabled(false);
                    myBackupDatabase.setEnabled(false);
                    myDatabaseMaintenance.setEnabled(false);
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(datasources, ignoreTimestamps);
                            } catch (DatabaseJobRunningException e) {
                                LOGGER.error("There was already a database job running!", e);
                            }
                        }
                    }).start();
                }
            }).show(getWindow());
        } else if (clickEvent.getSource() == myRemoveImages) {
            new SinglePanelWindow(50, Sizeable.UNITS_EM, null, getApplication().getBundleString("datasourceSelection.caption"), new DatasourcesSelectionPanel(false) {
                @Override
                protected void onContinue(final Collection<DatasourceConfig> datasources, final boolean ignoreTimestamps) {
                    myUpdateDatabase.setEnabled(false);
                    myRemoveImages.setEnabled(false);
                    myResetDatabase.setEnabled(false);
                    myBackupDatabase.setEnabled(false);
                    myDatabaseMaintenance.setEnabled(false);
                    new Thread(new Runnable() {
                        public void run() {
                            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningImageRemoval"));
                            MyTunesRss.EXECUTOR_SERVICE.cancelImageGenerators();
                            try {
                                OrphanedImageRemover orphanedImageRemover = new OrphanedImageRemover();
                                orphanedImageRemover.init();
                                try {
                                    MyTunesRss.STORE.executeStatement(new RemoveImagesForDataSourcesStatement(MyTunesRssUtils.toDatasourceIds(datasources)));
                                    MyTunesRss.STORE.executeStatement(new RecreateHelpTablesStatement(true, false, false, true));
                                    orphanedImageRemover.remove();
                                } finally {
                                    orphanedImageRemover.destroy();
                                }
                            } catch (SQLException e) {
                                LOGGER.error("Could not remove images.", e);
                            } finally {
                                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
                                MyTunesRss.EXECUTOR_SERVICE.scheduleImageGenerators();
                            }
                        }
                    }).start();
                }
            }).show(getWindow());
        } else if (clickEvent.getSource() == myStopDatabaseUpdate) {
            myUpdateDatabase.setEnabled(false);
            myRemoveImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(false);
            myResetDatabase.setEnabled(false);
            myBackupDatabase.setEnabled(false);
            myDatabaseMaintenance.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.cancelDatabaseUpdate();
        } else if (clickEvent.getSource() == myResetDatabase) {
            myUpdateDatabase.setEnabled(false);
            myRemoveImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(false);
            myResetDatabase.setEnabled(false);
            myBackupDatabase.setEnabled(false);
            myDatabaseMaintenance.setEnabled(false);
            try {
                MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseReset();
            } catch (DatabaseJobRunningException e) {
                LOGGER.error("There was already a database job running!", e);
            }
        } else if (clickEvent.getSource() == myBackupDatabase) {
            myUpdateDatabase.setEnabled(false);
            myRemoveImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(false);
            myResetDatabase.setEnabled(false);
            myBackupDatabase.setEnabled(false);
            myDatabaseMaintenance.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseBackup();
        } else if (clickEvent.getSource() == myDatabaseMaintenance) {
            myUpdateDatabase.setEnabled(false);
            myRemoveImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(false);
            myResetDatabase.setEnabled(false);
            myBackupDatabase.setEnabled(false);
            myDatabaseMaintenance.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseMaintenance();
        } else if (clickEvent.getSource() == myHelp) {
            getWindow().open(new ExternalResource("http://kb.mytunesrss.com"));
        } else if (clickEvent.getSource() == myQuitMyTunesRss) {
            final Button yes = new Button(getApplication().getBundleString("button.yes"));
            Button no = new Button(getApplication().getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getApplication().getBundleString("statusPanel.warn.quit.caption"), getApplication().getBundleString("statusPanel.warn.quit.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        Label label = new Label(StatusPanel.this.getApplication().getBundleString("statusPanel.info.quitMyTunesRss"));
                        label.setSizeFull();
                        label.addStyleName("goodbye");
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(label);
                        new Thread(new Runnable() {
                            public void run() {
                                MyTunesRssUtils.shutdownGracefully();
                            }
                        }, "AsyncWebAdminShutdown").start();
                    }
                }
            }.show(getWindow());
        }
    }

    public void handleEvent(MyTunesRssEvent event) {
        Application application = getApplication();
        if (application != null) {
            synchronized (application) {
                if (event.getType() == MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED) {
                    myUpdateDatabase.setEnabled(false);
                    myRemoveImages.setEnabled(false);
                    myStopDatabaseUpdate.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseMaintenanceRunning());
                    myResetDatabase.setEnabled(false);
                    myBackupDatabase.setEnabled(false);
                    myDatabaseMaintenance.setEnabled(false);
                    myDatabaseStatus.setValue(MyTunesRssUtils.getBundleString(getLocale(), event.getMessageKey(), event.getMessageParams()));
                } else if (event.getType() == MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED) {
                    myUpdateDatabase.setEnabled(true);
                    myRemoveImages.setEnabled(true);
                    myStopDatabaseUpdate.setEnabled(false);
                    myResetDatabase.setEnabled(true);
                    myBackupDatabase.setEnabled(MyTunesRss.CONFIG.isDefaultDatabase());
                    myDatabaseMaintenance.setEnabled(true);
                    myDatabaseStatus.setValue(getLastDatabaseUpdateText());
                } else if (event.getType() == MyTunesRssEvent.EventType.SERVER_STARTED) {
                    myStartServer.setEnabled(false);
                    myStopServer.setEnabled(true);
                    myServerStatus.setValue(getApplication().getBundleString("statusPanel.serverRunning"));
                } else if (event.getType() == MyTunesRssEvent.EventType.SERVER_STOPPED) {
                    myStartServer.setEnabled(true);
                    myStopServer.setEnabled(false);
                    myServerStatus.setValue(getApplication().getBundleString("statusPanel.serverStopped"));
                }
            }
        }
    }

    private String getLastDatabaseUpdateText() {
        try {
            SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
            if (systemInformation.getLastUpdate() > 0) {
                Date date = new Date(systemInformation.getLastUpdate());
                return getApplication().getBundleString("statusPanel.lastDatabaseUpdate") + " " + new SimpleDateFormat(
                        getApplication().getBundleString("statusPanel.lastDatabaseUpdateDateFormat")).format(date);
            } else {
                return getApplication().getBundleString("statusPanel.databaseNotYetCreated");
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get last update time from database.", e);
            }
        }
        return getApplication().getBundleString("statusPanel.databaseStatusUnknown");
    }

    public void refresh(Refresher source) {
        refreshAlert();
        // refresh internal addresses
        myInternalAddresses.removeAllItems();
        if (MyTunesRss.WEBSERVER.isRunning()) {
            for (String address : getLocalAddresses()) {
                myInternalAddresses.addItem(new Object[]{address}, address);
            }
        }
        myInternalAddresses.setPageLength(Math.min(myInternalAddresses.getItemIds().size(), 10));
        // refresh external address
        myExternalAddresses.removeAllItems();
        if (MyTunesRss.WEBSERVER.isRunning()) {
            String address = FetchExternalAddressRunnable.EXTERNAL_ADDRESS;
            if (StringUtils.isNotEmpty(address) && !"unreachable".equals(address)) {
                myExternalAddresses.addItem(new Object[]{address}, address);
            } else {
                myExternalAddresses.addItem(new Object[]{getApplication().getBundleString("statusPanel.externalAddressUnavailable")}, "unavailable");
            }
        }
        myExternalAddresses.setPageLength(myExternalAddresses.getItemIds().size());
        // refresh active connections
        myConnections.removeAllItems();
        Map<String, SessionInfo> infos = new HashMap<>();
        for (MyTunesRssSessionInfo session : MyTunesRss.WEBSERVER.getSessionInfos()) {
            if (session.getUser() != null) {
                String key = session.getBestRemoteAddress() + session.getUser().getName();
                SessionInfo info = infos.get(key);
                if (info == null) {
                    info = new SessionInfo();
                    info.remoteAddress = session.getBestRemoteAddress();
                    info.userName = session.getUser().getName();
                    infos.put(key, info);
                }
                info.lastAccessTime = Math.max(info.lastAccessTime, session.getLastAccessTime());
                info.bytesStreamed += session.getBytesStreamed();
                info.sessions++;
            }
        }
        for (SessionInfo info : infos.values()) {
            myConnections.addItem(new Object[]{info.remoteAddress, info.userName, info.sessions, new Date(info.lastAccessTime), MyTunesRssUtils.getMemorySizeForDisplay(info.bytesStreamed)}, info.remoteAddress + info.userName);
        }
        myConnections.setPageLength(Math.min(myConnections.getItemIds().size(), 15));
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).checkUnhandledException();
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).checkImportantMessage();
    }

    private void refreshAlert() {
        myAlertPanel.setVisible(false);
        myAlertPanel.removeAllComponents();
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());
    }

    private String[] getLocalAddresses() {
        if (StringUtils.isBlank(MyTunesRss.CONFIG.getHost())) {
            String[] addresses = NetworkUtils.getLocalNetworkAddresses();
            for (int i = 0; i < addresses.length; i++) {
                addresses[i] = "http://" + addresses[i] + ":" + MyTunesRss.CONFIG.getPort();
            }
            return addresses;
        } else {
            return new String[] {
                    "http://" + MyTunesRss.CONFIG.getHost() + ":" + MyTunesRss.CONFIG.getPort()
            };
        }
    }

    public static class SessionInfo {
        public String remoteAddress;
        public String userName;
        public long sessions;
        public long lastAccessTime;
        public long bytesStreamed;
    }
}
