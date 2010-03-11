/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.ComponentFactory;

public class StatusPanel extends Panel implements Button.ClickListener {

    private ComponentFactory myComponentFactory;
    private Label myServerStatus;
    private Button myStartServer;
    private Button myStopServer;
    private Button myServerInfo;
    private Label myDatabaseStatus;
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

    public StatusPanel(Application application, ComponentFactory componentFactory) {
        super(componentFactory.createVerticalLayout(true, true));
        myComponentFactory = componentFactory;
        init(application);
        initFromConfig(application);
    }

    protected void init(Application application) {
        setCaption(MyTunesRssWebAdminUtils.getBundleString("statusPanel.caption"));
        addComponent(new Embedded("", new ClassResource("mytunesrss.png", application)));
        Panel server = new Panel(MyTunesRssWebAdminUtils.getBundleString("statusPanel.server.caption"), myComponentFactory.createVerticalLayout(true, true));
        addComponent(server);
        TextField textField = myComponentFactory.createTextField("statusPanel.server.status"); // TODO status
        textField.setReadOnly(true);
        textField.setWidth("100%");
        server.addComponent(textField);
        Panel serverButtons = new Panel(myComponentFactory.createHorizontalLayout(false, true));
        serverButtons.addStyleName("light");
        server.addComponent(serverButtons);
        myStartServer = myComponentFactory.createButton("statusPanel.server.start", StatusPanel.this);
        myStopServer = myComponentFactory.createButton("statusPanel.server.stop", StatusPanel.this);
        myServerInfo = myComponentFactory.createButton("statusPanel.server.info", StatusPanel.this);
        serverButtons.addComponent(myStartServer);
        serverButtons.addComponent(myStopServer);
        serverButtons.addComponent(myServerInfo);
        Panel database = new Panel(MyTunesRssWebAdminUtils.getBundleString("statusPanel.database.caption"), myComponentFactory.createVerticalLayout(true, true));
        addComponent(database);
        textField = myComponentFactory.createTextField("statusPanel.database.status"); // TODO status
        textField.setReadOnly(true);
        textField.setWidth("100%");
        database.addComponent(textField);
        Panel databaseButtons = new Panel(myComponentFactory.createHorizontalLayout(false, true));
        databaseButtons.addStyleName("light");
        database.addComponent(databaseButtons);
        myUpdateDatabase = myComponentFactory.createButton("statusPanel.database.update", StatusPanel.this);
        myResetDatabase = myComponentFactory.createButton("statusPanel.database.reset", StatusPanel.this);
        databaseButtons.addComponent(myUpdateDatabase);
        databaseButtons.addComponent(myResetDatabase);
        Panel configButtons = new Panel(MyTunesRssWebAdminUtils.getBundleString("statusPanel.config.caption"), myComponentFactory.createGridLayout(4, 3, true, true));
        addComponent(configButtons);
        myServerConfig = myComponentFactory.createButton("statusPanel.config.server", StatusPanel.this);
        myDatabaseConfig = myComponentFactory.createButton("statusPanel.config.database", StatusPanel.this);
        myDatasourcesConfig = myComponentFactory.createButton("statusPanel.config.datasources", StatusPanel.this);
        myDataimportConfig = myComponentFactory.createButton("statusPanel.config.dataimport", StatusPanel.this);
        myContentConfig = myComponentFactory.createButton("statusPanel.config.contents", StatusPanel.this);
        myUsersConfig = myComponentFactory.createButton("statusPanel.config.users", StatusPanel.this);
        myNotificationsConfig = myComponentFactory.createButton("statusPanel.config.notifications", StatusPanel.this);
        myStatisticsConfig = myComponentFactory.createButton("statusPanel.config.statistics", StatusPanel.this);
        myMiscConfig = myComponentFactory.createButton("statusPanel.config.misc", StatusPanel.this);
        myStreamingConfig = myComponentFactory.createButton("statusPanel.config.streaming", StatusPanel.this);
        myAddonsConfig = myComponentFactory.createButton("statusPanel.config.addons", StatusPanel.this);
        mySupportConfig = myComponentFactory.createButton("statusPanel.config.support", StatusPanel.this);
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
        Panel buttons = new Panel(myComponentFactory.createHorizontalLayout(false, true));
        buttons.addStyleName("light");
        addComponent(buttons);
        myHelp = myComponentFactory.createButton("statusPanel.help", StatusPanel.this);
        myLogout = myComponentFactory.createButton("statusPanel.logout", StatusPanel.this);
        buttons.addComponent(myHelp);
        buttons.addComponent(myLogout);
    }

    private void initFromConfig(Application application) {
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        MyTunesRssWebAdmin application = ((MyTunesRssWebAdmin) getApplication());
        if (clickEvent.getButton() == myLogout) {
            application.close();
        } else if (clickEvent.getButton() == myServerConfig) {
            application.setMainComponent(new ServerConfigPanel(getApplication(), myComponentFactory));
        } else if (clickEvent.getButton() == myDatabaseConfig) {
            application.setMainComponent(new DatabaseConfigPanel(getApplication(), myComponentFactory));
        } else if (clickEvent.getButton() == myDatasourcesConfig) {
            application.setMainComponent(new DatasourcesConfigPanel(getApplication(), myComponentFactory));
        } else if (clickEvent.getButton() == myDataimportConfig) {
            application.setMainComponent(new DataImportConfigPanel(getApplication(), myComponentFactory));
        } else if (clickEvent.getButton() == myContentConfig) {
            application.setMainComponent(new ContentConfigPanel(getApplication(), myComponentFactory));
        }
    }
}
