/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class MigrationStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(MigrationStatement.class);

    public void execute(Connection connection) throws SQLException {
        String version = getVersion(connection);
        if (LOG.isInfoEnabled()) {
            LOG.info("Database version " + version + " detected.");
        }
    }

    private String getVersion(Connection connection) throws SQLException {
        ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "getVersion").executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("version");
        }
        throw new RuntimeException("Could not get current version for migration!");
    }
}