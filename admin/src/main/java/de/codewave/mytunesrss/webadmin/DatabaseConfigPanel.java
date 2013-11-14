/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.DatabaseType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class DatabaseConfigPanel extends MyTunesRssConfigPanel implements Property.ValueChangeListener {

    private Form myDatabaseTypeForm;
    private Form myDatabaseBackupForm;
    private Select myDatabaseType;
    private SmartTextField myDatabaseDriver;
    private SmartTextField myDatabaseConnection;
    private SmartTextField myConnectionOptions;
    private SmartTextField myDatabaseUser;
    private SmartTextField myDatabasePassword;
    private Table myUpdateTriggers;
    private Table myBackupTriggers;
    private Button myAddUpdateTrigger;
    private Button myAddBackupTrigger;
    private SmartTextField myNumberKeepBackups;
    private CheckBox myBackupAfterInit;
    private Button myHelpButton;

    public void attach() {
        super.attach();
        init(getBundleString("databaseConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myDatabaseType = getComponentFactory().createSelect("databaseConfigPanel.databaseType", Arrays.asList(DatabaseType.h2, DatabaseType.mysqlinternal, DatabaseType.h2custom, DatabaseType.postgres, DatabaseType.mysql));
        myDatabaseType.addListener(this);
        myDatabaseDriver = getComponentFactory().createTextField("databaseConfigPanel.databaseDriver");
        myDatabaseConnection = getComponentFactory().createTextField("databaseConfigPanel.databaseConnection");
        myConnectionOptions = getComponentFactory().createTextField("databaseConfigPanel.databaseConnectionOptions");
        myDatabaseUser = getComponentFactory().createTextField("databaseConfigPanel.databaseUser");
        myDatabasePassword = getComponentFactory().createPasswordTextField("databaseConfigPanel.databasePassword");
        myHelpButton = getComponentFactory().createButton("databaseConfigPanel.help", this);
        myUpdateTriggers = new Table();
        myUpdateTriggers.setCacheRate(50);
        myUpdateTriggers.addContainerProperty("day", Select.class, null, getBundleString("databaseConfigPanel.cronTriggers.day"), null, null);
        myUpdateTriggers.addContainerProperty("hour", Select.class, null, getBundleString("databaseConfigPanel.cronTriggers.hour"), null, null);
        myUpdateTriggers.addContainerProperty("minute", Select.class, null, getBundleString("databaseConfigPanel.cronTriggers.minute"), null, null);
        myUpdateTriggers.addContainerProperty("delete", Button.class, null, "", null, null);
        myUpdateTriggers.setEditable(true);
        myBackupTriggers = new Table();
        myBackupTriggers.setCacheRate(50);
        myBackupTriggers.addContainerProperty("day", Select.class, null, getBundleString("databaseConfigPanel.cronTriggers.day"), null, null);
        myBackupTriggers.addContainerProperty("hour", Select.class, null, getBundleString("databaseConfigPanel.cronTriggers.hour"), null, null);
        myBackupTriggers.addContainerProperty("minute", Select.class, null, getBundleString("databaseConfigPanel.cronTriggers.minute"), null, null);
        myBackupTriggers.addContainerProperty("delete", Button.class, null, "", null, null);
        myBackupTriggers.setEditable(true);
        myAddUpdateTrigger = getComponentFactory().createButton("databaseConfigPanel.addSchedule", this);
        myAddBackupTrigger = getComponentFactory().createButton("databaseConfigPanel.addSchedule", this);
        myNumberKeepBackups = getComponentFactory().createTextField("databaseConfigPanel.numberKeepBackup", getValidatorFactory().createMinMaxValidator(1, 25));
        myBackupAfterInit = getComponentFactory().createCheckBox("databaseConfigPanel.backupAfterInit");

        myDatabaseTypeForm = getComponentFactory().createForm(null, true);
        myDatabaseTypeForm.addField(myDatabaseType, myDatabaseType);
        myDatabaseTypeForm.addField(myDatabaseDriver, myDatabaseDriver);
        myDatabaseTypeForm.addField(myDatabaseConnection, myDatabaseConnection);
        myDatabaseTypeForm.addField(myConnectionOptions, myConnectionOptions);
        myDatabaseTypeForm.addField(myDatabaseUser, myDatabaseUser);
        myDatabaseTypeForm.addField(myDatabasePassword, myDatabasePassword);
        myDatabaseTypeForm.addField(myHelpButton, myHelpButton);
        addComponent(getComponentFactory().surroundWithPanel(myDatabaseTypeForm, FORM_PANEL_MARGIN_INFO, getBundleString("databaseConfigPanel.caption.database")));

        Panel schedulesPanel = new Panel(getBundleString("databaseConfigPanel.caption.updateTriggers"), getComponentFactory().createVerticalLayout(true, true));
        schedulesPanel.addComponent(myUpdateTriggers);
        schedulesPanel.addComponent(myAddUpdateTrigger);
        addComponent(schedulesPanel);

        schedulesPanel = new Panel(getBundleString("databaseConfigPanel.caption.backupTriggers"), getComponentFactory().createVerticalLayout(true, true));
        schedulesPanel.addComponent(myBackupTriggers);
        schedulesPanel.addComponent(myAddBackupTrigger);
        addComponent(schedulesPanel);

        myDatabaseBackupForm = getComponentFactory().createForm(null, true);
        myDatabaseBackupForm.addField(myNumberKeepBackups, myNumberKeepBackups);
        myDatabaseBackupForm.addField(myBackupAfterInit, myBackupAfterInit);
        addComponent(getComponentFactory().surroundWithPanel(myDatabaseBackupForm, FORM_PANEL_MARGIN_INFO, getBundleString("databaseConfigPanel.caption.databaseBackupOptions")));

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        DatabaseType databaseType = MyTunesRss.CONFIG.getDatabaseType();
        myDatabaseType.select(databaseType);
        if (databaseType != DatabaseType.h2 && databaseType != DatabaseType.h2custom) {
            myDatabaseDriver.setValue(MyTunesRss.CONFIG.getDatabaseDriver());
        } else {
            myDatabaseDriver.setValue(null);
        }
        if (databaseType != DatabaseType.h2) {
            myConnectionOptions.setValue(MyTunesRss.CONFIG.getDatabaseConnectionOptions());
        }
        if (databaseType != DatabaseType.h2 && databaseType != DatabaseType.mysqlinternal) {
            myDatabaseConnection.setValue(MyTunesRss.CONFIG.getDatabaseConnection());
            myDatabaseUser.setValue(MyTunesRss.CONFIG.getDatabaseUser());
            myDatabasePassword.setValue(MyTunesRss.CONFIG.getDatabasePassword());
        } else {
            myDatabaseConnection.setValue(null);
            myDatabaseUser.setValue(null);
            myDatabasePassword.setValue(null);
        }
        showHideDatabaseDetails(databaseType);
        showHideDatabaseBackup(databaseType);
        refreshUpdateTriggers();
        refreshBackupTriggers();
        myNumberKeepBackups.setValue(MyTunesRss.CONFIG.getNumberKeepDatabaseBackups());
        myBackupAfterInit.setValue(MyTunesRss.CONFIG.isBackupDatabaseAfterInit());
    }

    private void refreshUpdateTriggers() {
        myUpdateTriggers.removeAllItems();
        for (String trigger : MyTunesRss.CONFIG.getDatabaseUpdateTriggers()) {
            addTrigger(trigger, myUpdateTriggers);
        }
        setTriggersPageLength(myUpdateTriggers);
    }

    private void refreshBackupTriggers() {
        myBackupTriggers.removeAllItems();
        for (String trigger : MyTunesRss.CONFIG.getDatabaseBackupTriggers()) {
            addTrigger(trigger, myBackupTriggers);
        }
        setTriggersPageLength(myBackupTriggers);
    }

    private void addTrigger(String trigger, Table table) {
        String[] cronTriggerParts = trigger.split(" ");
        Select daySelect = getComponentFactory().createSelect(null, Arrays.asList(MyTunesRssJobUtils.getDays()));
        daySelect.select(new MyTunesRssJobUtils.TriggerItem(cronTriggerParts[5], null));
        Select hourSelect = getComponentFactory().createSelect(null, Arrays.asList(MyTunesRssJobUtils.getHours()));
        hourSelect.select(new MyTunesRssJobUtils.TriggerItem(cronTriggerParts[2], null));
        Select minuteSelect = getComponentFactory().createSelect(null, Arrays.asList(MyTunesRssJobUtils.getMinutes()));
        minuteSelect.select(new MyTunesRssJobUtils.TriggerItem(cronTriggerParts[1], null));
        Button deleteButton = new Button(getBundleString("button.delete"), this);
        table.addItem(new Object[]{daySelect, hourSelect, minuteSelect, deleteButton}, myItemIdGenerator.getAndIncrement());
        setTriggersPageLength(table);
    }

    private void setTriggersPageLength(Table table) {
        table.setPageLength(Math.min(table.getItemIds().size(), 10));
    }

    private void showHideDatabaseBackup(DatabaseType type) {
        myBackupTriggers.setEnabled(type == DatabaseType.h2);
        myAddBackupTrigger.setEnabled(type == DatabaseType.h2);
        myNumberKeepBackups.setEnabled(type == DatabaseType.h2);
        myBackupAfterInit.setEnabled(type == DatabaseType.h2);
    }

    private void showHideDatabaseDetails(DatabaseType type) {
        myDatabaseDriver.setEnabled(type != DatabaseType.h2 && type != DatabaseType.h2custom);
        myDatabaseConnection.setEnabled(type != DatabaseType.h2 && type != DatabaseType.mysqlinternal);
        myConnectionOptions.setEnabled(type != DatabaseType.h2);
        myDatabaseUser.setEnabled(type != DatabaseType.h2 && type != DatabaseType.mysqlinternal);
        myDatabasePassword.setEnabled(type != DatabaseType.h2 && type != DatabaseType.mysqlinternal);
        setOptional(myConnectionOptions);
        if (type == DatabaseType.h2) {
            setOptional(myDatabaseDriver);
            setOptional(myDatabaseConnection);
            setOptional(myDatabaseUser);
            setOptional(myDatabasePassword);
        } else if (type == DatabaseType.h2custom) {
            setOptional(myDatabaseDriver);
            setRequired(myDatabaseConnection);
            setRequired(myDatabaseUser);
            setRequired(myDatabasePassword);
        } else if (type == DatabaseType.mysqlinternal) {
            setRequired(myDatabaseDriver);
            setOptional(myDatabaseConnection);
            setOptional(myDatabaseUser);
            setOptional(myDatabasePassword);
        } else {
            setRequired(myDatabaseDriver);
            setRequired(myDatabaseConnection);
            setRequired(myDatabaseUser);
            setRequired(myDatabasePassword);
        }
    }

    protected void writeToConfig() {
        //noinspection OverlyStrongTypeCast
        MyTunesRss.CONFIG.setDatabaseType((DatabaseType) myDatabaseType.getValue());
        MyTunesRss.CONFIG.setDatabaseDriver(myDatabaseDriver.getStringValue(null));
        MyTunesRss.CONFIG.setDatabaseConnection(myDatabaseConnection.getStringValue(null));
        MyTunesRss.CONFIG.setDatabaseConnectionOptions(myConnectionOptions.getStringValue(null));
        MyTunesRss.CONFIG.setDatabaseUser(myDatabaseUser.getStringValue(null));
        MyTunesRss.CONFIG.setDatabasePassword(myDatabasePassword.getStringValue(null));
        List<String> updateTriggers = new ArrayList<String>();
        for (Object itemId : myUpdateTriggers.getItemIds()) {
            updateTriggers.add("0 " + getTableCellString(myUpdateTriggers, itemId, "minute") + " " + getTableCellString(myUpdateTriggers, itemId, "hour") + " ? * " + getTableCellString(myUpdateTriggers, itemId, "day"));
        }
        MyTunesRss.CONFIG.setDatabaseUpdateTriggers(updateTriggers);
        List<String> backupTriggers = new ArrayList<String>();
        for (Object itemId : myBackupTriggers.getItemIds()) {
            backupTriggers.add("0 " + getTableCellString(myBackupTriggers, itemId, "minute") + " " + getTableCellString(myBackupTriggers, itemId, "hour") + " ? * " + getTableCellString(myBackupTriggers, itemId, "day"));
        }
        MyTunesRss.CONFIG.setDatabaseBackupTriggers(backupTriggers);
        MyTunesRss.CONFIG.setNumberKeepDatabaseBackups(myNumberKeepBackups.getIntegerValue(5));
        MyTunesRss.CONFIG.setBackupDatabaseAfterInit(myBackupAfterInit.booleanValue());
        MyTunesRssJobUtils.scheduleDatabaseJob();
        MyTunesRss.CONFIG.save();
    }

    private String getTableCellString(Table table, Object itemId, String property) {
        Select select = (Select) table.getItem(itemId).getItemProperty(property).getValue();
        MyTunesRssJobUtils.TriggerItem triggerItem = (MyTunesRssJobUtils.TriggerItem) select.getValue();
        return triggerItem.getKey();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myHelpButton) {
            getWindow().open(new ExternalResource("http://kb.mytunesrss.com/dbhelp"));
        } else if (clickEvent.getSource() == myAddUpdateTrigger) {
            addTrigger("0 0 0 ? * SUN-SAT", myUpdateTriggers);
        } else if (clickEvent.getSource() == myAddBackupTrigger) {
            addTrigger("0 0 0 ? * SUN-SAT", myBackupTriggers);
        } else {
            final Object updateTriggerToDelete = getTriggerToDelete(clickEvent.getSource(), myUpdateTriggers);
            final Object backupTriggerToDelete = getTriggerToDelete(clickEvent.getSource(), myBackupTriggers);
            if (updateTriggerToDelete != null || backupTriggerToDelete != null) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("databaseConfigDialog.optionWindowDeleteCronTrigger.caption"), getBundleString("databaseConfigDialog.optionWindowDeleteCronTrigger.message"), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            if (updateTriggerToDelete != null) {
                                myUpdateTriggers.removeItem(updateTriggerToDelete);
                                setTriggersPageLength(myUpdateTriggers);
                            } else {
                                myBackupTriggers.removeItem(backupTriggerToDelete);
                                setTriggersPageLength(myBackupTriggers);
                            }
                        }
                    }
                }.show(getWindow());
            } else {
                super.buttonClick(clickEvent);
            }
        }
    }

    protected boolean beforeSave() {
        if (VaadinUtils.isValid(myDatabaseTypeForm)) {
            String errorKey = validateDatabaseConnection();
            if (StringUtils.isNotBlank(errorKey)) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError(errorKey);
            } else {
                if (isDatabaseChanged()) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showWarning("databaseConfigPanel.warning.databaseChanged");
                }
                return true;
            }
        } else {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return false;
    }

    private String validateDatabaseConnection() {
        if (myDatabaseType.getValue() != DatabaseType.h2 && myDatabaseType.getValue() != DatabaseType.h2custom) {
            String driverClass = myDatabaseDriver.getStringValue(null);
            try {
                Class.forName(driverClass, false, MyTunesRss.EXTRA_CLASSLOADER);
            } catch (ClassNotFoundException e) {
                return "databaseConfigPanel.error.missingDriverClass";
            }
            if (myDatabaseType.getValue() == DatabaseType.mysqlinternal) {
                try {
                    Class.forName("com.mysql.management.MysqldResource", false, MyTunesRss.EXTRA_CLASSLOADER);
                } catch (ClassNotFoundException e) {
                    return "databaseConfigPanel.error.missingMxjConnector";
                }
                try {
                    if (MyTunesRss.EXTRA_CLASSLOADER.getResource("connector-mxj.properties") == null) {
                        return "databaseConfigPanel.error.missingMxjDbFiles";
                    }
                } catch (MissingResourceException e) {
                    return "databaseConfigPanel.error.missingMxjDbFiles";
                }
            }
        }
        return null;
    }

    private Object getTriggerToDelete(Object source, Table table) {
        for (Object itemId : table.getItemIds()) {
            if (source == table.getItem(itemId).getItemProperty("delete").getValue()) {
                return itemId;
            }
        }
        return null;
    }

    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
        if (((EventObject) valueChangeEvent).getSource() == myDatabaseType) {
            showHideDatabaseDetails((DatabaseType) valueChangeEvent.getProperty().getValue());
            showHideDatabaseBackup((DatabaseType) valueChangeEvent.getProperty().getValue());
            fillDefaultDatabaseProperties();
        }
    }

    private void fillDefaultDatabaseProperties() {
        switch ((DatabaseType) myDatabaseType.getValue()) {
            case h2custom:
                myDatabaseDriver.setValue("");
                myDatabaseConnection.setValue("jdbc:h2:file:{path_to_folder}");
                myConnectionOptions.setValue("");
                myDatabaseUser.setValue(null);
                myDatabasePassword.setValue(null);
                break;
            case mysql:
                myDatabaseDriver.setValue("com.mysql.jdbc.Driver");
                myDatabaseConnection.setValue("jdbc:mysql://{database server host}/{database_name}");
                myConnectionOptions.setValue("");
                myDatabaseUser.setValue(null);
                myDatabasePassword.setValue(null);
                break;
            case mysqlinternal:
                myDatabaseDriver.setValue("com.mysql.jdbc.Driver");
                myDatabaseConnection.setValue("");
                myConnectionOptions.setValue(MyTunesRssConfig.DEFAULT_INTERNAL_MYSQL_CONNECTION_OPTIONS);
                myDatabaseUser.setValue("");
                myDatabasePassword.setValue("");
                break;
            case postgres:
                myDatabaseDriver.setValue("org.postgresql.Driver");
                myDatabaseConnection.setValue("jdbc:postgresql://{database server host}/{database name}");
                myConnectionOptions.setValue("");
                myDatabaseUser.setValue(null);
                myDatabasePassword.setValue(null);
                break;
            default:
                myDatabaseDriver.setValue("");
                myDatabaseConnection.setValue("");
                myConnectionOptions.setValue("");
                myDatabaseUser.setValue("");
                myDatabasePassword.setValue("");
        }
    }

    private boolean isDatabaseChanged() {
        DatabaseType newType = (DatabaseType) myDatabaseType.getValue();
        String newDriver = StringUtils.trimToEmpty((String) myDatabaseDriver.getValue());
        String newConnect = StringUtils.trimToEmpty((String) myDatabaseConnection.getValue());
        String newConnectOptions = StringUtils.trimToEmpty((String) myConnectionOptions.getValue());
        String newUser = StringUtils.trimToEmpty((String) myDatabaseUser.getValue());
        String newPass = StringUtils.trimToEmpty((String) myDatabasePassword.getValue());
        if (!newType.equals(MyTunesRss.CONFIG.getDatabaseType())) {
            return true;
        }
        if (!newDriver.equals(MyTunesRss.CONFIG.getDatabaseDriver())) {
            return true;
        }
        if (!newConnect.equals(MyTunesRss.CONFIG.getDatabaseConnection())) {
            return true;
        }
        if (!newConnectOptions.equals(MyTunesRss.CONFIG.getDatabaseConnectionOptions())) {
            return true;
        }
        if (!newUser.equals(MyTunesRss.CONFIG.getDatabaseUser())) {
            return true;
        }
        if (!newPass.equals(MyTunesRss.CONFIG.getDatabasePassword())) {
            return true;
        }
        return false;
    }
}
