/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.MaintenanceStatement;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.task.BackupDatabaseRunnable
 */
public class DatabaseMaintenanceRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMaintenanceRunnable.class);

    public void run() {
        MyTunesRss.EXECUTOR_SERVICE.cancelImageGenerators();
        try {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseMaintenance");
            MyTunesRssEventManager.getInstance().fireEvent(event);
            MyTunesRss.LAST_DATABASE_EVENT.set(event);
            MyTunesRss.STORE.executeStatement(new MaintenanceStatement());
        } catch (SQLException e) {
            LOGGER.error("Error during database maintenance.", e);
        } finally {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
            MyTunesRss.EXECUTOR_SERVICE.scheduleImageGenerators();
        }
    }
}