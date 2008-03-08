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
        // migration < 3.0 to current version
        if (databaseVersion.compareTo(new Version("3.0")) < 0) {
            MyTunesRssUtils.createStatement(connection, "dropAllTablesForPre30Migration").execute();
            new CreateAllTablesStatement().execute(connection);
        } else {
            // migration from 3.0.x to 3.1-EAP-1
            if (databaseVersion.compareTo(new Version("3.1-EAP-1")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate30to31eap1").execute();
                databaseVersion = new Version("3.1-EAP-1");
            }
            // migration from 3.1-EAP-1 to 3.1-EAP-4
            if (databaseVersion.compareTo(new Version("3.1-EAP-4")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap131eap4").execute();
                databaseVersion = new Version("3.1-EAP-4");
            }
            // migration from 3.1-EAP-4 to 3.1-EAP-6
            if (databaseVersion.compareTo(new Version("3.1-EAP-6")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap4to31eap6").execute();
                databaseVersion = new Version("3.1-EAP-6");
            }
            // migration from 3.1-EAP-6 to 3.1-EAP-9
            if (databaseVersion.compareTo(new Version("3.1-EAP-9")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap6to31eap9").execute();
                databaseVersion = new Version("3.1-EAP-9");
            }
            // migration from 3.1-EAP-9 to 3.1-EAP-11
            if (databaseVersion.compareTo(new Version("3.1-EAP-11")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap9to31eap11").execute();
                databaseVersion = new Version("3.1-EAP-11");
            }
            // migration from 3.1-EAP-11 to 3.1-EAP-12
            if (databaseVersion.compareTo(new Version("3.1-EAP-12")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap11to31eap12").execute();
                databaseVersion = new Version("3.1-EAP-12");
            }
            // migration from 3.1-EAP-12 to 3.1-EAP-16
            if (databaseVersion.compareTo(new Version("3.1-EAP-16")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap12to31eap16").execute();
                databaseVersion = new Version("3.1-EAP-16");
            }
            // migration from 3.1-EAP-16 to 3.1-EAP-17
            if (databaseVersion.compareTo(new Version("3.1-EAP-17")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap16to31eap17").execute();
                databaseVersion = new Version("3.1-EAP-17");
            }
            // migration from 3.1-EAP-17 to 3.1-EAP-20
            if (databaseVersion.compareTo(new Version("3.1-EAP-20")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate31eap17to31eap20").execute();
                databaseVersion = new Version("3.1-EAP-20");
            }
            // migration for 3.2-EAP-1
            if (databaseVersion.compareTo(new Version("3.2-EAP-1")) < 0) {
                MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_1").execute();
                databaseVersion = new Version("3.2-EAP-1");
            }
        }
        new UpdateDatabaseVersionStatement().execute(connection);
    }

    private String getVersion(Connection connection) throws SQLException {
        ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "getVersion").executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("version");
        }
        throw new RuntimeException("Could not get current version for migration!");
    }
}