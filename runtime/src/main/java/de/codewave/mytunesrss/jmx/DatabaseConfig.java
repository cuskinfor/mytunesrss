/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.sql.*;

import javax.management.*;
import java.sql.*;
import java.text.*;
import java.util.Date;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.jmx.DatabaseConfig
 */
public class DatabaseConfig extends MyTunesRssMBean implements DatabaseConfigMBean, MyTunesRssEventListener {
    private static final Log LOG = LogFactory.getLog(DatabaseConfig.class);

    private String myCurrentUpdateAction;

    DatabaseConfig() throws NotCompliantMBeanException {
        super(DatabaseConfigMBean.class);
        MyTunesRssEventManager.getInstance().addListener(this);
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
        if (!DatabaseBuilderTask.isRunning()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final DatabaseBuilderTask databaseBuilderTask = MyTunesRss.createDatabaseBuilderTask();
                        databaseBuilderTask.execute();
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error during database update", e);
                        }
                    }
                }
            }).start();
            onChange();
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateStarted");
        } else {
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateAlreadyRunning");
        }
    }

    public String getDatabaseStatus() {
        if (myCurrentUpdateAction != null) {
            return myCurrentUpdateAction;
        }
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
            if (systemInformation.getLastUpdate() > 0) {
                Date date = new Date(systemInformation.getLastUpdate());
                return MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRssUtils.getBundleString(
                        "settings.lastDatabaseUpdateDateFormat")).format(date);
            } else {
                return MyTunesRssUtils.getBundleString("settings.databaseNotYetCreated");
            }
        } catch (SQLException e) {
            return e.getMessage();
        } finally {
            session.commit();
        }
    }

    public boolean isUpdateOnServerStart() {
        return MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart();
    }

    public void setUpdateOnServerStart(boolean updateOnServerStart) {
        MyTunesRss.CONFIG.setUpdateDatabaseOnServerStart(updateOnServerStart);
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

    public void handleEvent(MyTunesRssEvent event) {
        if (event == MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED) {
            myCurrentUpdateAction = MyTunesRssUtils.getBundleString(event.getMessageKey());
        } else if (event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED || event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED_NOT_RUN) {
            myCurrentUpdateAction = null;
        }
    }

    public boolean isIgnoreCoverArtworkFromFiles() {
        return MyTunesRss.CONFIG.isIgnoreArtwork();
    }

    public void setIgnoreCoverArtworkFromFiles(boolean ignoreCoverArtwork) {
        MyTunesRss.CONFIG.setIgnoreArtwork(ignoreCoverArtwork);
        onChange();
    }
}