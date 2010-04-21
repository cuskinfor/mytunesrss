/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.henrik.refresher.Refresher;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusPanel extends Panel implements Button.ClickListener, MyTunesRssEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(StatusPanel.class);

    private Label myServerStatus;
    private Label myDatabaseStatus;
    private Label myMyTunesRssComStatus;
    private Button myStartServer;
    private Button myStopServer;
    private Button myServerInfo;
    private Button myUpdateDatabase;
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

    public void attach() {
        MyTunesRssEventManager.getInstance().addListener(this);
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        setCaption(getApplication().getBundleString("statusPanel.caption"));
        Embedded logo = new Embedded("", new ClassResource("mytunesrss.png", getApplication()));
        logo.setWidth(241, Sizeable.UNITS_PIXELS);
        logo.setHeight(88, Sizeable.UNITS_PIXELS);
        addComponent(logo);
        Panel server = new Panel(getApplication().getBundleString("statusPanel.server.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(server);
        myServerStatus = new Label();
        myServerStatus.setWidth("100%");
        myServerStatus.setStyleName("statusmessage");
        server.addComponent(myServerStatus);
        Panel serverButtons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        serverButtons.addStyleName("light");
        server.addComponent(serverButtons);
        myStartServer = getApplication().getComponentFactory().createButton("statusPanel.server.start", StatusPanel.this);
        myStopServer = getApplication().getComponentFactory().createButton("statusPanel.server.stop", StatusPanel.this);
        myServerInfo = getApplication().getComponentFactory().createButton("statusPanel.server.info", StatusPanel.this);
        serverButtons.addComponent(myStartServer);
        serverButtons.addComponent(myStopServer);
        serverButtons.addComponent(myServerInfo);
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
        myResetDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.reset", StatusPanel.this);
        databaseButtons.addComponent(myUpdateDatabase);
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
        buttons.addComponent(myHelp);
        buttons.addComponent(myLogout);
        myRefresher = new Refresher();
        addComponent(myRefresher);

        initFromConfig();
    }

    @Override
    public void detach() {
        MyTunesRssEventManager.getInstance().removeListener(this);
    }

    private void initFromConfig() {
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());
        myServerStatus.setValue(MyTunesRss.WEBSERVER.isRunning() ? getApplication().getBundleString("statusPanel.serverRunning") : getApplication().getBundleString("statusPanel.serverStopped"));
        myDatabaseStatus.setValue(MyTunesRssExecutorService.isDatabaseUpdateRunning() ? null : getLastDatabaseUpdateText());
        myMyTunesRssComStatus.setValue(getApplication().getBundleString("statusPanel.myTunesRssComStateUnknown"));
        myRefresher.setRefreshInterval(2500);
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        MyTunesRssWebAdmin application = getApplication();
        if (clickEvent.getButton() == myLogout) {
            application.close();
        } else if (clickEvent.getButton() == myServerConfig) {
            application.setMainComponent(new ServerConfigPanel());
        } else if (clickEvent.getButton() == myDatabaseConfig) {
            application.setMainComponent(new DatabaseConfigPanel());
        } else if (clickEvent.getButton() == myDatasourcesConfig) {
            application.setMainComponent(new DatasourcesConfigPanel());
        } else if (clickEvent.getButton() == myDataimportConfig) {
            application.setMainComponent(new DataImportConfigPanel());
        } else if (clickEvent.getButton() == myContentConfig) {
            application.setMainComponent(new ContentConfigPanel());
        } else if (clickEvent.getButton() == myUsersConfig) {
            application.setMainComponent(new UserConfigPanel());
        } else if (clickEvent.getButton() == myNotificationsConfig) {
            application.setMainComponent(new AdminNotificationsConfigPanel());
        } else if (clickEvent.getButton() == myStatisticsConfig) {
            application.setMainComponent(new StatisticsConfigPanel());
        } else if (clickEvent.getButton() == myMiscConfig) {
            application.setMainComponent(new MiscConfigPanel());
        } else if (clickEvent.getButton() == myStreamingConfig) {
            application.setMainComponent(new StreamingConfigPanel());
        } else if (clickEvent.getButton() == myAddonsConfig) {
            application.setMainComponent(new AddonsConfigPanel());
        } else if (clickEvent.getButton() == mySupportConfig) {
            application.setMainComponent(new SupportConfigPanel());
        } else if (clickEvent.getSource() == myStartServer) {
            myStartServer.setEnabled(false);
            MyTunesRss.startWebserver();
        } else if (clickEvent.getSource() == myStopServer) {
            myStopServer.setEnabled(false);
            MyTunesRss.stopWebserver();
        } else if (clickEvent.getSource() == myUpdateDatabase) {
            myUpdateDatabase.setEnabled(false);
            myResetDatabase.setEnabled(false);
            MyTunesRssExecutorService.scheduleDatabaseUpdate();
        } else if (clickEvent.getSource() == myResetDatabase) {
            myUpdateDatabase.setEnabled(false);
            myResetDatabase.setEnabled(false);
            MyTunesRssExecutorService.scheduleDatabaseReset();
        }
    }

    public void handleEvent(MyTunesRssEvent event) {
        Application application = getApplication();
        if (application != null) {
            synchronized (application) {
                if (event.getType() == MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED) {
                    myUpdateDatabase.setEnabled(false);
                    myResetDatabase.setEnabled(false);
                    myDatabaseStatus.setValue(MyTunesRssUtils.getBundleString(getLocale(), event.getMessageKey(), event.getMessageParams()));
                } else if (event.getType() == MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED) {
                    myUpdateDatabase.setEnabled(true);
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
                } else if (event.getType() == MyTunesRssEvent.EventType.MYTUNESRSS_COM_UPDATED) {
                    myMyTunesRssComStatus.setValue(MyTunesRssUtils.getBundleString(getLocale(), event.getMessageKey(), event.getMessageParams()));
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
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get last update time from database.", e);
            }
        } finally {
            session.commit();
        }
        return getApplication().getBundleString("statusPanel.databaseStatusUnknown");
    }
}
