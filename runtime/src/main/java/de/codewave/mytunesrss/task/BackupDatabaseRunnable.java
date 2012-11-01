/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.BackupDatabaseRunnable
 */
public class BackupDatabaseRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupDatabaseRunnable.class);

    public void run() {
        MyTunesRss.EXECUTOR_SERVICE.cancelImageGenerators();
        try {
            MyTunesRssUtils.backupDatabase();
            MyTunesRssUtils.removeAllButLatestDatabaseBackups(MyTunesRss.CONFIG.getNumberKeepDatabaseBackups());
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        } catch (SQLException e) {
            LOGGER.error("Error while backing up database.", e);
        } catch (IOException e) {
            LOGGER.error("Error while backing up database.", e);
        } finally {
            MyTunesRss.EXECUTOR_SERVICE.scheduleImageGenerators();
        }
    }
}