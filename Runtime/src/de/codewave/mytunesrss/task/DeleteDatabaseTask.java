/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.utils.*;

import java.io.*;
import java.sql.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class DeleteDatabaseTask extends MyTunesRssTask {
    private boolean myDeleteOnNextStartOnError;

    public DeleteDatabaseTask(boolean deleteOnNextStartOnError) {
        myDeleteOnNextStartOnError = deleteOnNextStartOnError;
    }

    public void execute() throws IOException, SQLException {
        String pathname = PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        if (MyTunesRssUtils.deleteRecursivly(new File(pathname + "/" + MyTunesRssDataStore.DIRNAME))) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("deleteDatabaseOnNextStartOnError", false);
        } else {
            if (myDeleteOnNextStartOnError) {
                Preferences.userRoot().node("/de/codewave/mytunesrss").putBoolean("deleteDatabaseOnNextStartOnError", true);
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.deleteDatabaseOnNextStartOnError"));
            }
        }
    }
}