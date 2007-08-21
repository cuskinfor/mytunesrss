/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.*;
import org.apache.commons.logging.*;

import java.sql.*;

import com.sun.java_cup.internal.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class MigrationStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(MigrationStatement.class);

    public void execute(Connection connection) throws SQLException {
        Version databaseVersion = new Version(getVersion(connection));
        if (LOG.isInfoEnabled()) {
            LOG.info("Database version " + databaseVersion + " detected.");
        }
        // migration from 3.0.x to 3.1-EAP-1
        if (databaseVersion.compareTo(new Version("3.1-EAP-1")) < 0) {
            new DropAllTablesStatement().execute(connection);
            MyTunesRssUtils.createStatement(connection, "migrate30to31eap1").execute();
            new CreateAllTablesStatement().execute(connection);
            databaseVersion = new Version("3.1-EAP-1");
            new UpdateDatabaseVersionStatement().execute(connection);
        }
        // migration from 3.1-EAP-1 to 3.1-EAP-4
        if (databaseVersion.compareTo(new Version("3.1-EAP-4")) < 0) {
            new DropAllTablesStatement().execute(connection);
            MyTunesRssUtils.createStatement(connection, "migrate31eap131eap4").execute();
            new CreateAllTablesStatement().execute(connection);
            databaseVersion = new Version("3.1-EAP-4");
            new UpdateDatabaseVersionStatement().execute(connection);
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