/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;

import javax.management.*;
import java.sql.*;
import java.text.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.jmx.DatabaseConfig
 */
public class DatabaseConfig extends MyTunesRssMBean implements DatabaseConfigMBean {

    DatabaseConfig() throws NotCompliantMBeanException {
        super(DatabaseConfigMBean.class);
    }

    public boolean isIgnoreTimestampsOnUpdate() {
        return MyTunesRss.CONFIG.isIgnoreTimestamps();
    }

    public void setIgnoreTimestampsOnUpdate(boolean ignoreTimestamps) {
        MyTunesRss.CONFIG.setIgnoreTimestamps(ignoreTimestamps);
        onChange();
    }

    public String resetDatabase() {
        MyTunesRssUtils.executeTask(null, null, null, false, new RecreateDatabaseTask());
        onChange();
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
            onChange();
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateStarted");
        } else {
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateAlreadyRunning");
        }
    }

    public String getDatabaseStatus() {
        if (MyTunesRss.createDatabaseBuilderTask().isRunning()) {
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateRunning");
        }
        try {
            SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
            if (systemInformation.getLastUpdate() > 0) {
                Date date = new Date(systemInformation.getLastUpdate());
                return MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRssUtils.getBundleString(
                        "settings.lastDatabaseUpdateDateFormat")).format(date);
            } else {
                return MyTunesRssUtils.getBundleString("settings.databaseNotYetCreated");
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
        onChange();
    }

    public boolean isAutoUpdate() {
        return MyTunesRss.CONFIG.isAutoUpdateDatabase();
    }

    public void setAutoUpdate(boolean autoUpdate) {
        MyTunesRss.CONFIG.setAutoUpdateDatabase(autoUpdate);
        onChange();
    }

    public int getAutoUpdateIntervalMinutes() {
        return MyTunesRss.CONFIG.getAutoUpdateDatabaseInterval();
    }

    public void setAutoUpdateIntervalMinutes(int minutes) {
        MyTunesRss.CONFIG.setAutoUpdateDatabaseInterval(minutes);
        onChange();
    }

    public String getArtistDropWords() {
        return MyTunesRss.CONFIG.getArtistDropWords();
    }

    public void setArtistDropWords(String artistDropWords) {
        MyTunesRss.CONFIG.setArtistDropWords(artistDropWords);
        onChange();
    }

    public String getFileTypes() {
        return MyTunesRss.CONFIG.getFileTypes();
    }

    public void setFileTypes(String fileTypes) {
        MyTunesRss.CONFIG.setFileTypes(fileTypes);
        onChange();
    }

    public boolean isRemoveMissingItunesTracks() {
        return MyTunesRss.CONFIG.isItunesDeleteMissingFiles();
    }

    public void setRemoveMissingItunesTracks(boolean removeMissingTracks) {
        MyTunesRss.CONFIG.setItunesDeleteMissingFiles(removeMissingTracks);
        onChange();
    }
}