/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;

import java.io.*;
import java.sql.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class DropAllTablesTask extends MyTunesRssTask {
    public void execute() throws SQLException {
        try {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.begin();
            storeSession.executeStatement(new DropAllTablesStatement());
            storeSession.commit();
        } catch (SQLException e) {
            if (System.getProperty("database.type") == null) {
                Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).putBoolean("deleteDatabaseOnNextStartOnError", true);
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.deleteDatabaseOnNextStartOnError"));
            } else {
                throw e;
            }
        }
    }

}