/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class MaintenanceStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(MaintenanceStatement.class);

    @Override
    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "maintenance");
        StopWatch.start("Database maintenance");
        try {
            statement.execute();
        } finally {
            StopWatch.stop();
        }
    }
}
