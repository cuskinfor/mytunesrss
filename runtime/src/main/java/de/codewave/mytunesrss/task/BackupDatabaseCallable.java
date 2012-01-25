/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;

import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.BackupDatabaseCallable
 */
public class BackupDatabaseCallable implements Callable<Void> {
    public Void call() throws Exception {
        MyTunesRssUtils.backupDatabase();
        MyTunesRssUtils.removeAllButLatestDatabaseBackups(MyTunesRss.CONFIG.getNumberKeepDatabaseBackups());
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        return null;
    }
}