/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;

import java.sql.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class DropAllTablesTask extends MyTunesRssTask {
    public void execute() throws SQLException {
        try {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.executeStatement(new DropAllTablesStatement());
            DatabaseBuilderTask.doCheckpoint(storeSession, true);
        } catch (SQLException e) {
            if (MyTunesRss.CONFIG.isDefaultDatabase()) {
                MyTunesRss.CONFIG.setDeleteDatabaseOnNextStartOnError(true);
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.deleteDatabaseOnNextStartOnError"));
            } else {
                throw e;
            }
        }
    }

}