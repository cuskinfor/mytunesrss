/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.SmartTextField;

public class StatusPanel extends Panel implements Button.ClickListener {

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

    public void attach() {
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        setCaption(getApplication().getBundleString("statusPanel.caption"));
        Embedded logo = new Embedded("", new ClassResource("mytunesrss.png", getApplication()));
        logo.setWidth(241, Sizeable.UNITS_PIXELS);
        logo.setHeight(88, Sizeable.UNITS_PIXELS);
        addComponent(logo);
        Panel server = new Panel(getApplication().getBundleString("statusPanel.server.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(server);
        SmartTextField textField = getApplication().getComponentFactory().createTextField("statusPanel.server.status"); // TODO status
        textField.setEnabled(false);
        textField.setWidth("100%");
        server.addComponent(textField);
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
        textField = getApplication().getComponentFactory().createTextField("statusPanel.database.status"); // TODO status
        textField.setEnabled(false);
        textField.setWidth("100%");
        database.addComponent(textField);
        Panel databaseButtons = new Panel(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        databaseButtons.addStyleName("light");
        database.addComponent(databaseButtons);
        myUpdateDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.update", StatusPanel.this);
        myResetDatabase = getApplication().getComponentFactory().createButton("statusPanel.database.reset", StatusPanel.this);
        databaseButtons.addComponent(myUpdateDatabase);
        databaseButtons.addComponent(myResetDatabase);
        Panel mytunesrss = new Panel(getApplication().getBundleString("statusPanel.mytunesrss.caption"), getApplication().getComponentFactory().createVerticalLayout(true, true));
        addComponent(mytunesrss);
        textField = getApplication().getComponentFactory().createTextField("statusPanel.mytunesrss.status"); // TODO status
        textField.setEnabled(false);
        textField.setWidth("100%");
        mytunesrss.addComponent(textField);
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
        initFromConfig();
    }

    private void initFromConfig() {
        myStartServer.setEnabled(!MyTunesRss.WEBSERVER.isRunning());
        myStopServer.setEnabled(MyTunesRss.WEBSERVER.isRunning());
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
        }
    }
}
