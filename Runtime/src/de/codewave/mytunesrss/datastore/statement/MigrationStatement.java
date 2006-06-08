/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class MigrationStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(MigrationStatement.class);

    public void execute(Connection connection) throws SQLException {
        String version = getVersion(connection);
        if (version.equals("2.0")) {
            // update from 2.0 to 2.0.1
            if (LOG.isInfoEnabled()) {
                LOG.info("Migrating database to version 2.0.1");
            }
            connection.createStatement().execute("DROP TABLE artist");
            connection.createStatement().execute("DROP TABLE album");
            connection.createStatement().execute(
                    "CREATE CACHED TABLE album ( name VARCHAR(255) NOT NULL, first_char VARCHAR(1), track_count INTEGER, artist_count INTEGER, artist VARCHAR(255) )");
            connection.createStatement().execute(
                    "CREATE CACHED TABLE artist ( name VARCHAR(255) NOT NULL, first_char VARCHAR(1), track_count INTEGER, album_count INTEGER )");
            connection.createStatement().execute("UPDATE system_information SET version = '2.0.1'");
            version = "2.0.1";
        }
    }

    private String getVersion(Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT version AS version FROM system_information");
        if (resultSet.next()) {
            return resultSet.getString("version");
        }
        throw new RuntimeException("Could not get current version for migration!");
    }
}