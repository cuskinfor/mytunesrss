/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.utils.Version;
import de.codewave.utils.network.NetworkUtils;
import de.codewave.utils.network.UpdateInfo;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.henrik.refresher.Refresher;

import javax.xml.bind.JAXB;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatusPanel extends Panel implements Button.ClickListener, MyTunesRssEventListener, Refresher.RefreshListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusPanel.class);

    private Panel myAlertPanel;
    private Label myServerStatus;
    private Label myDatabaseStatus;
    private Label myMyTunesRssComStatus;
    private Button myStartServer;
    private Button myStopServer;
    private Table myInternalAddresses;
    private Table myExternalAddresses;
    private Table myConnections;
    private Button myUpdateDatabase;
    private Button myFullUpdateDatabase;
    private Button myUpdateImages;
    private Button myStopDatabaseUpdate;
    private Button myResetDatabase;
    private Button myServerConfig;
    private Button myDatabaseConfig;
    private Button myDatasourcesConfig;
    private Button myDataimportConfig;
    private Button myContentConfig;
    private Button myUsersConfig;
    private Button myNotificationsConfig;
    private Button myStatisticsConfig;
    private Button myMiscConfig;
    private Button myStreamingConfig;
    private Button myAddonsConfig;
    private Button mySupportConfig;
    private Button myHelp;
    private Button myLogout;
    private Refresher myRefresher;
    private Panel myUpdatePanel;
    private Button myQuitMyTunesRss;
    private MessageOfTheDayItem myMessageOfTheDay;
    private long myMessageOfTheDayLastRefresh;

    public void attach() {
        super.attach();
        MyTunesRssEventManager.getInstance().addListener(this);
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        setCaption(getApplication().getBundleString("statusPanel.caption"));
        Embedded logo = new Embedded("", new ClassResource("mytunesrss.png", getApplication()));
        logo.setWidth(290, Sizeable.UNITS_PIXELS);
        logo.setHeight(88, Sizeable.UNITS_PIXELS);
        addComponent(logo);
        myAlertPanel = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        myAlertPanel.setStyleName("alertPanel");
        myAlertPanel.setVisible(false);
        addComponent(myAlertPanel);
        myUpdatePanel = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(myUpdatePanel);
        Panel server = new Panel(getApplication().getBundleString("statusPanel.server.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(server);
        ((Layout) server.getContent()).setMargin(true);
        ((Layout.SpacingHandler) server.getContent()).setSpacing(true);
        Panel serverAccordionPanel = new Panel();
        Accordion accordion = new Accordion();
        serverAccordionPanel.setContent(accordion);
        server.addComponent(serverAccordionPanel);
        Panel serverGeneral = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        accordion.addTab(serverGeneral, getApplication().getBundleString("statusPanel.serverGeneral.caption"), null);
        myServerStatus = new Label();
        myServerStatus.setWidth("100%");
        myServerStatus.setStyleName("statusmessage");
        serverGeneral.addComponent(myServerStatus);
        Panel serverButtons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        serverButtons.addStyleName("light");
        serverGeneral.addComponent(serverButtons);
        myStartServer = getApplication().getComponentFactory().createButton("statusPanel.server.start", StatusPanel.this);
        myStopServer = getApplication().getComponentFactory().createButton("statusPanel.server.stop", StatusPanel.this);
        serverButtons.addComponent(myStartServer);
        serverButtons.addComponent(myStopServer);
        Panel serverAddresses = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        myInternalAddresses = new Table();
        myInternalAddresses.addContainerProperty("address", String.class, null, getApplication().getBundleString("statusPanel.internalServerAddresses"), null, null);
        myInternalAddresses.setPageLength(0);
        myExternalAddresses = new Table();
        myExternalAddresses.addContainerProperty("address", String.class, null, getApplication().getBundleString("statusPanel.externalServerAddress"), null, null);
        myExternalAddresses.setPageLength(0);
        serverAddresses.addComponent(myInternalAddresses);
        serverAddresses.addComponent(myExternalAddresses);
        accordion.addTab(serverAddresses, getApplication().getBundleString("statusPanel.serverAddresses.caption"), null);
        Panel serverConnections = new Panel(null, getApplication().getComponentFactory().createVerticalLayout(true, true));
        myConnections = new Table();
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
        myDatabaseStatus.setStyleName("statusmessage");
        database.addComponent(myDatabaseStatus);
        Panel databaseButtons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        databaseButtons.addStyleName("light");
        database.addComponent(databaseButtons);
        myUpdateDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.update", StatusPanel.this);
        myFullUpdateDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.fullupdate", StatusPanel.this);
        myUpdateImages = getApplication().getComponentFactory().createButton("statusPanel.database.imageUpdate", StatusPanel.this);
        myStopDatabaseUpdate = getApplication().getComponentFactory().createButton("statusPanel.database.stopUpdate", StatusPanel.this);
        myResetDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.reset", StatusPanel.this);
        databaseButtons.addComponent(myUpdateDatabase);
        databaseButtons.addComponent(myFullUpdateDatabase);
        databaseButtons.addComponent(myUpdateImages);
        databaseButtons.addComponent(myStopDatabaseUpdate);
        databaseButtons.addComponent(myResetDatabase);
        Panel mytunesrss = new Panel(getApplication().getBundleString("statusPanel.mytunesrss.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(mytunesrss);
        myMyTunesRssComStatus = new Label();
        myMyTunesRssComStatus.setWidth("100%");
        myMyTunesRssComStatus.setStyleName("statusmessage");
        mytunesrss.addComponent(myMyTunesRssComStatus);
        Panel configButtons = new Panel(getApplication().getBundleString("statusPanel.config.caption"), getApplication().getComponentFactory().createGridLayout(4, 3, true, true));
        addComponent(configButtons);
        myServerConfig = getApplication().getComponentFactory().createButton("statusPanel.config.server", StatusPanel.this);
        myDatabaseConfig = getApplication().getComponentFactory().createButton("statusPanel.config.database", StatusPanel.this);
        myDatasourcesConfig = getApplication().getComponentFactory().createButton("statusPanel.config.datasources", StatusPanel.this);
        myDataimportConfig = getApplication().getComponentFactory().createButton("statusPanel.config.dataimport", StatusPanel.this);
        myContentConfig = getApplication().getComponentFactory().createButton("statusPanel.config.contents", StatusPanel.this);
        myUsersConfig = getApplication().getComponentFactory().createButton("statusPanel.config.users", StatusPanel.this);
        myNotificationsConfig = getApplication().getComponentFactory().createButton("statusPanel.config.notifications", StatusPanel.this);
        myStatisticsConfig = getApplication().getComponentFactory().createButton("statusPanel.config.statistics", StatusPanel.this);
        myMiscConfig = getApplication().getComponentFactory().createButton("statusPanel.config.misc", StatusPanel.this);
        myStreamingConfig = getApplication().getComponentFactory().createButton("statusPanel.config.streaming", StatusPanel.this);
        myAddonsConfig = getApplication().getComponentFactory().createButton("statusPanel.config.addons", StatusPanel.this);
        mySupportConfig = getApplication().getComponentFactory().createButton("statusPanel.config.support", StatusPanel.this);
        configButtons.addComponent(myServerConfig);
        configButtons.addComponent(myDatabaseConfig);
        configButtons.addComponent(myDatasourcesConfig);
        configButtons.addComponent(myDataimportConfig);
        configButtons.addComponent(myContentConfig);
        configButtons.addComponent(myUsersConfig);
        configButtons.addComponent(myNotificationsConfig);
        configButtons.addComponent(myStatisticsConfig);
        configButtons.addComponent(myMiscConfig);
        configButtons.addComponent(myStreamingConfig);
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
        myDatabaseStatus.setValue(MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning() ? null : getLastDatabaseUpdateText());
        myRefresher = new Refresher();
        addComponent(myRefresher);
        myRefresher.setRefreshInterval(MyTunesRssWebAdmin.ADMIN_REFRESHER_INTERVAL_MILLIS);
        myRefresher.addListener(this);
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());
        myUpdateDatabase.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning());
        myFullUpdateDatabase.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning());
        myUpdateImages.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning());
        myStopDatabaseUpdate.setEnabled(MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning());
        myResetDatabase.setEnabled(!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning());
        refreshMyTunesRssComUpdateState();
        refreshMyTunesUpdateInfo();
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
        } else if (clickEvent.getButton() == myDataimportConfig) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(new DataImportConfigPanel());
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
            myUpdateDatabase.setEnabled(false);
            myFullUpdateDatabase.setEnabled(false);
            myUpdateImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(true);
            myResetDatabase.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.isIgnoreTimestamps());
        } else if (clickEvent.getSource() == myFullUpdateDatabase) {
            myUpdateDatabase.setEnabled(false);
            myFullUpdateDatabase.setEnabled(false);
            myUpdateImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(true);
            myResetDatabase.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(true);
        } else if (clickEvent.getSource() == myUpdateImages) {
            myUpdateDatabase.setEnabled(false);
            myFullUpdateDatabase.setEnabled(false);
            myUpdateImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(true);
            myResetDatabase.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.scheduleImageUpdate();
        } else if (clickEvent.getSource() == myStopDatabaseUpdate) {
            myUpdateDatabase.setEnabled(false);
            myFullUpdateDatabase.setEnabled(false);
            myUpdateImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(false);
            myResetDatabase.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.cancelDatabaseJob();
        } else if (clickEvent.getSource() == myResetDatabase) {
            myUpdateDatabase.setEnabled(false);
            myFullUpdateDatabase.setEnabled(false);
            myUpdateImages.setEnabled(false);
            myStopDatabaseUpdate.setEnabled(false);
            myResetDatabase.setEnabled(false);
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseReset();
        } else if (clickEvent.getSource() == myHelp) {
            getWindow().open(new ExternalResource("http://docs.codewave.de/mytunesrss4"));
        } else if (clickEvent.getSource() == myQuitMyTunesRss) {
            final Button yes = new Button(getApplication().getBundleString("button.yes"));
            Button no = new Button(getApplication().getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getApplication().getBundleString("statusPanel.warn.quit.caption"), getApplication().getBundleString("statusPanel.warn.quit.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        Label label = new Label(StatusPanel.this.getApplication().getBundleString("statusPanel.info.quitMyTunesRss"));
                        label.setSizeFull();
                        label.setStyleName("goodbye");
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(label);
                        MyTunesRss.EXECUTOR_SERVICE.schedule(new Runnable() {
                            public void run() {
                                MyTunesRssUtils.shutdownGracefully();
                            }
                        }, 2, TimeUnit.SECONDS);
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
                    myFullUpdateDatabase.setEnabled(false);
                    myUpdateImages.setEnabled(false);
                    myStopDatabaseUpdate.setEnabled(true);
                    myResetDatabase.setEnabled(false);
                    myDatabaseStatus.setValue(MyTunesRssUtils.getBundleString(getLocale(), event.getMessageKey(), event.getMessageParams()));
                } else if (event.getType() == MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED) {
                    myUpdateDatabase.setEnabled(true);
                    myFullUpdateDatabase.setEnabled(true);
                    myUpdateImages.setEnabled(true);
                    myStopDatabaseUpdate.setEnabled(false);
                    myResetDatabase.setEnabled(true);
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
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            final SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
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
        } finally {
            session.commit();
        }
        return getApplication().getBundleString("statusPanel.databaseStatusUnknown");
    }

    public void refresh(Refresher source) {
        refreshMyTunesRssComUpdateState();
        refreshMyTunesUpdateInfo();
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
        Map<String, SessionInfo> infos = new HashMap<String, SessionInfo>();
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
    }

    private void refreshMyTunesRssComUpdateState() {
        MyTunesRssEvent lastMyTunesRssComEvent = MyTunesRssComUpdateRunnable.LAST_UPDATE_EVENT;
        if (lastMyTunesRssComEvent == null) {
            myMyTunesRssComStatus.setValue(getApplication().getBundleString("statusPanel.myTunesRssComStateUnknown"));
        } else {
            myMyTunesRssComStatus.setValue(MyTunesRssUtils.getBundleString(getLocale(), lastMyTunesRssComEvent.getMessageKey(), lastMyTunesRssComEvent.getMessageParams()));
        }
    }

    private void refreshMyTunesUpdateInfo() {
        UpdateInfo updateInfo = CheckUpdateRunnable.UPDATE_INFO;
        myUpdatePanel.removeAllComponents();
        Label updateStatusLabel = new Label();
        myUpdatePanel.addComponent(updateStatusLabel);
        updateStatusLabel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        updateStatusLabel.setStyleName("statusmessage");
        myUpdatePanel.setVisible(false);
        if (updateInfo != null) {
            Version updateVersion = new Version(updateInfo.getVersion());
            if (updateVersion.compareTo(new Version(MyTunesRss.VERSION)) > 0) {
                try {
                    String osIdentifier = "Unknown";
                    if (SystemUtils.IS_OS_MAC_OSX) {
                        osIdentifier = "MacOSX";
                    } else if (SystemUtils.IS_OS_WINDOWS) {
                        osIdentifier = "Windows";
                    }
                    Link downloadLink = new Link(getApplication().getBundleString("statusPanel.update.download"), new ExternalResource(updateInfo.getUrl(osIdentifier)));
                    updateStatusLabel.setValue(getApplication().getBundleString("statusPanel.updates.info", MyTunesRss.VERSION, updateInfo.getVersion()));
                    myUpdatePanel.addComponent(new Label(updateInfo.getInfo(getApplication().getLocale()), Label.CONTENT_PREFORMATTED));
                    if (MyTunesRss.REGISTRATION.isValidVersion(updateVersion)) {
                        myUpdatePanel.addComponent(downloadLink);
                    } else {
                        Label maxVersionInfoLabel = new Label(getApplication().getBundleString("statusPanel.updates.maxVersionLimit"));
                        maxVersionInfoLabel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
                        maxVersionInfoLabel.setStyleName("statusmessage");
                        myUpdatePanel.addComponent(maxVersionInfoLabel);
                    }
                    myUpdatePanel.setStyleName("updatePanel");
                    myUpdatePanel.setVisible(true);
                } catch (MalformedURLException e) {
                    // ignore, panel remains invisible
                }
            }
        }
    }

    private void refreshAlert() {
        myAlertPanel.setVisible(false);
        myAlertPanel.removeAllComponents();
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());

        // message of the day
        if (myMessageOfTheDay == null || (System.currentTimeMillis() - myMessageOfTheDayLastRefresh > 1000 * 60 * 15)) {
            myMessageOfTheDayLastRefresh = System.currentTimeMillis();
            // refresh every 15 minutes<
            try {
                URI uri = MyTunesRss.REGISTRATION.isReleaseVersion() && !MyTunesRss.REGISTRATION.isUnregistered() ? new URI("http://www.codewave.de/tools/motd/mytunesrss.xml") : new URI("http://www.codewave.de/tools/motd/mytunesrss_unregistered.xml");
                MessageOfTheDay motd = JAXB.unmarshal(uri, MessageOfTheDay.class);
                if (motd != null && motd.getItems() != null) {
                    for (MessageOfTheDayItem item : motd.getItems()) {
                        if (!StringUtils.isBlank(item.getValue())) {
                            Version minVersion = StringUtils.isNotBlank(item.getMinVersion()) ? new Version(item.getMinVersion()) : new Version(0, 0, 0);
                            Version maxVersion = StringUtils.isNotBlank(item.getMaxVersion()) ? new Version(item.getMaxVersion()) : new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
                            Version currentVersion = new Version(MyTunesRss.VERSION);
                            if (minVersion.compareTo(new Version(MyTunesRss.VERSION)) <= 0 && maxVersion.compareTo(currentVersion) >= 0) {
                                if (StringUtils.equalsIgnoreCase(getLocale().getLanguage(), item.getLanguage())) {
                                    myMessageOfTheDay = item;
                                    break;
                                } else if (StringUtils.isBlank(item.getLanguage())) {
                                    myMessageOfTheDay = item;
                                }
                            }
                        }
                    }
                }
            } catch (URISyntaxException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not read and parse message of the day from Codewave server.", e);
                }
            }
        }
        if (myMessageOfTheDay != null) {
            myAlertPanel.setVisible(true);
            myAlertPanel.addComponent(new Label(StringUtils.trim(myMessageOfTheDay.getValue()), Label.CONTENT_XHTML));
        }

        // registration feedback
        RegistrationFeedback feedback = MyTunesRssUtils.getRegistrationFeedback(getLocale());
        if (feedback != null && feedback.getMessage() != null) {
            myAlertPanel.setVisible(true);
            myAlertPanel.addComponent(new Label(feedback.getMessage()));
            if (!feedback.isValid()) {
                myStartServer.setEnabled(false);
                myStopServer.setEnabled(false);
                MyTunesRss.stopWebserver();
            }
        }

    }

    private String[] getLocalAddresses() {
        String[] addresses = NetworkUtils.getLocalNetworkAddresses();
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = "http://" + addresses[i] + ":" + MyTunesRss.CONFIG.getPort();
        }
        return addresses;
    }

    public static class SessionInfo {
        public String remoteAddress;
        public String userName;
        public long sessions;
        public long lastAccessTime;
        public long bytesStreamed;
    }
}
