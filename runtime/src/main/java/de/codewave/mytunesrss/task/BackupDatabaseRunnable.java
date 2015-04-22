/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.task.BackupDatabaseRunnable
 */
public class BackupDatabaseRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupDatabaseRunnable.class);

    @Override
    public void run() {
        try {
            MyTunesRssUtils.backupDatabase();
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        } catch (SQLException | IOException e) {
            LOGGER.error("Error while backing up database.", e);
        }
    }
}
