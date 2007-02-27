/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;

import java.sql.*;
import java.text.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.jmx.DatabaseConfig
 */
public class DatabaseConfig implements DatabaseConfigMBean {

    public boolean isIgnoreTimestampsOnUpdate() {
        return MyTunesRss.CONFIG.isIgnoreTimestamps();
    }

    public void setIgnoreTimestampsOnUpdate(boolean ignoreTimestamps) {
        MyTunesRss.CONFIG.setIgnoreTimestamps(ignoreTimestamps);
    }

    public String resetDatabase() {
        MyTunesRssUtils.executeTask(null, null, null, false, new RecreateDatabaseTask());
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }

    public synchronized String updateDatabase() {
        final DatabaseBuilderTask databaseBuilderTask = MyTunesRss.createDatabaseBuilderTask();
        if (!databaseBuilderTask.isRunning()) {
            new Thread(new Runnable() {
                public void run() {
                    MyTunesRssUtils.executeTask(null, null, null, false, databaseBuilderTask);
                }
            }).start();
            return MyTunesRss.BUNDLE.getString("jmx.databaseUpdateStarted");
        } else {
            return MyTunesRss.BUNDLE.getString("jmx.databaseUpdateAlreadyRunning");
        }
    }

    public String getDatabaseStatus() {
        if (MyTunesRss.createDatabaseBuilderTask().isRunning()) {
            return MyTunesRss.BUNDLE.getString("jmx.databaseUpdateRunning");
        }
        try {
            SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
            if (systemInformation.getLastUpdate() > 0) {
                Date date = new Date(systemInformation.getLastUpdate());
                return MyTunesRss.BUNDLE.getString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRss.BUNDLE.getString(
                        "settings.lastDatabaseUpdateDateFormat")).format(date);
            } else {
                return MyTunesRss.BUNDLE.getString("settings.databaseNotYetCreated");
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public boolean isUpdateOnServerStart() {
        return MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart();
    }

    public void setUpdateOnServerStart(boolean updateOnServerStart) {
        MyTunesRss.CONFIG.setUpdateDatabaseOnServerStart(updateOnServerStart);
    }

    public boolean isAutoUpdate() {
        return MyTunesRss.CONFIG.isAutoUpdateDatabase();
    }

    public void setAutoUpdate(boolean autoUpdate) {
        MyTunesRss.CONFIG.setAutoUpdateDatabase(autoUpdate);
    }

    public int getAutoUpdateIntervalMinutes() {
        return MyTunesRss.CONFIG.getAutoUpdateDatabaseInterval();
    }

    public void setAutoUpdateIntervalMinutes(int minutes) {
        MyTunesRss.CONFIG.setAutoUpdateDatabaseInterval(minutes);
    }
}