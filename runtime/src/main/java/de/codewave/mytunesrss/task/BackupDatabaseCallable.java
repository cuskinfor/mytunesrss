/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.BackupDatabaseCallable
 */
public class BackupDatabaseCallable implements Callable<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupDatabaseCallable.class);

    public Void call() throws Exception {
        MyTunesRssUtils.backupDatabase();
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        return null;
    }
}