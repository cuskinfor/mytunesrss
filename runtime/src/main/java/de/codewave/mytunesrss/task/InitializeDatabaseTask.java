/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;

import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class InitializeDatabaseTask extends MyTunesRssTask {
    private boolean myExistent;

    public void execute() throws IOException, SQLException {
        MyTunesRss.STORE.init();
        MyTunesRss.STORE.executeQuery(new DataStoreQuery<Boolean>() {
            public Boolean execute(Connection connection) throws SQLException {
                try {
                    ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "initialize").executeQuery();
                    if (resultSet.next()) {
                        myExistent = true;
                        return Boolean.TRUE;
                    }
                } catch (SQLException e) {
                    // intentionally left blank
                }
                myExistent = false;
                return Boolean.FALSE;
            }
        });
        if (!myExistent) {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.executeStatement(new CreateAllTablesStatement());
            storeSession.commit();
        } else {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.executeStatement(new MigrationStatement());
            storeSession.commit();
        }
    }

}

