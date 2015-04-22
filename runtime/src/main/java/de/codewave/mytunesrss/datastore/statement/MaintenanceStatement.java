/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreStatement;

import java.sql.Connection;
import java.sql.SQLException;

public class MaintenanceStatement implements DataStoreStatement {

    @Override
    public void execute(Connection connection) throws SQLException {
        StopWatch.start("Database maintenance");
        try {
            MyTunesRssUtils.createStatement(connection, "maintenance").execute();
        } finally {
            StopWatch.stop();
        }
        StopWatch.start("Updating statistics");
        try {
            MyTunesRssUtils.createStatement(connection, "updateStatistics").execute();
        } finally {
            StopWatch.stop();
        }
    }
}
