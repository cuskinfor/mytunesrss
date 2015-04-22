/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreStatement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateStatisticsStatement
 */
public class UpdateStatisticsStatement implements DataStoreStatement {
    @Override
    public void execute(Connection connection) throws SQLException {
        StopWatch.start("Updating statistics");
        try {
            MyTunesRssUtils.createStatement(connection, "updateStatistics").execute();
        } finally {
            StopWatch.stop();
        }
    }
}