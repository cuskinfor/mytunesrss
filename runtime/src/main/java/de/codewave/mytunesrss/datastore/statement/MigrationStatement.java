/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.Version;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class MigrationStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(MigrationStatement.class);

    public void execute(Connection connection) throws SQLException {
        Version databaseVersion = new Version(getVersion(connection));
        if (LOG.isInfoEnabled()) {
            LOG.info("Database version " + databaseVersion + " detected.");
        }
        if (databaseVersion.compareTo(new Version(MyTunesRss.VERSION)) < 0) {
            // migration < 3.0 to current version
            if (databaseVersion.compareTo(new Version("3.0")) < 0) {
                LOG.info("Migrating database to current version by droppping and creating all tables.");
                MyTunesRssUtils.createStatement(connection, "dropAllTablesForPre30Migration").execute();
                new CreateAllTablesStatement().execute(connection);
            } else {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(true);
                try {
                    // migration from 3.0.x to 3.1-EAP-1
                    if (databaseVersion.compareTo(new Version("3.1-EAP-1")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 1.");
                        MyTunesRssUtils.createStatement(connection, "migrate30to31eap1").execute();
                        databaseVersion = new Version("3.1-EAP-1");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-1 to 3.1-EAP-4
                    if (databaseVersion.compareTo(new Version("3.1-EAP-4")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 4.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap131eap4").execute();
                        databaseVersion = new Version("3.1-EAP-4");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-4 to 3.1-EAP-6
                    if (databaseVersion.compareTo(new Version("3.1-EAP-6")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 6.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap4to31eap6").execute();
                        databaseVersion = new Version("3.1-EAP-6");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-6 to 3.1-EAP-9
                    if (databaseVersion.compareTo(new Version("3.1-EAP-9")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 9.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap6to31eap9").execute();
                        databaseVersion = new Version("3.1-EAP-9");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-9 to 3.1-EAP-11
                    if (databaseVersion.compareTo(new Version("3.1-EAP-11")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 11.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap9to31eap11").execute();
                        databaseVersion = new Version("3.1-EAP-11");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-11 to 3.1-EAP-12
                    if (databaseVersion.compareTo(new Version("3.1-EAP-12")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 12.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap11to31eap12").execute();
                        databaseVersion = new Version("3.1-EAP-12");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-12 to 3.1-EAP-16
                    if (databaseVersion.compareTo(new Version("3.1-EAP-16")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 16.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap12to31eap16").execute();
                        databaseVersion = new Version("3.1-EAP-16");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-16 to 3.1-EAP-17
                    if (databaseVersion.compareTo(new Version("3.1-EAP-17")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 17.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap16to31eap17").execute();
                        databaseVersion = new Version("3.1-EAP-17");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration from 3.1-EAP-17 to 3.1-EAP-20
                    if (databaseVersion.compareTo(new Version("3.1-EAP-20")) < 0) {
                        LOG.info("Migrating database to 3.1 EAP 20.");
                        MyTunesRssUtils.createStatement(connection, "migrate31eap17to31eap20").execute();
                        databaseVersion = new Version("3.1-EAP-20");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.2-EAP-1
                    if (databaseVersion.compareTo(new Version("3.2-EAP-1")) < 0) {
                        LOG.info("Migrating database to 3.2 EAP 1.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_1").execute();
                        databaseVersion = new Version("3.2-EAP-1");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.2-EAP-10
                    if (databaseVersion.compareTo(new Version("3.2-EAP-10")) < 0) {
                        LOG.info("Migrating database to 3.2 EAP 10.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_10").execute();
                        databaseVersion = new Version("3.2-EAP-10");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.2-EAP-12
                    if (databaseVersion.compareTo(new Version("3.2-EAP-12")) < 0) {
                        LOG.info("Migrating database to 3.2 EAP 12.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_12").execute();
                        databaseVersion = new Version("3.2-EAP-12");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.2-EAP-13
                    if (databaseVersion.compareTo(new Version("3.2-EAP-13")) < 0) {
                        LOG.info("Migrating database to 3.2 EAP 13.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_13").execute();
                        databaseVersion = new Version("3.2-EAP-13");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.2-EAP-16
                    if (databaseVersion.compareTo(new Version("3.2-EAP-16")) < 0) {
                        LOG.info("Migrating database to 3.2 EAP 16.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_16_part1").execute();
                        MyTunesRssUtils.createStatement(connection, "migrate_3.2_eap_16_part2").execute();
                        databaseVersion = new Version("3.2-EAP-16");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.6-EAP-1
                    if (databaseVersion.compareTo(new Version("3.6-EAP-1")) < 0) {
                        LOG.info("Migrating database to 3.6 EAP 1.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.6_eap_1").execute();
                        databaseVersion = new Version("3.6-EAP-1");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.6-EAP-12
                    if (databaseVersion.compareTo(new Version("3.6-EAP-12")) < 0) {
                        LOG.info("Migrating database to 3.6 EAP 12.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.6_eap_12").execute();
                        databaseVersion = new Version("3.6-EAP-12");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.6
                    if (databaseVersion.compareTo(new Version("3.6")) < 0) {
                        LOG.info("Migrating database to 3.6.");
                        databaseVersion = new Version("3.6");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.7-EAP-1
                    if (databaseVersion.compareTo(new Version("3.7-EAP-1")) < 0) {
                        LOG.info("Migrating database to 3.7 EAP 1.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.7_eap_1_part1").execute();
                        MyTunesRssUtils.createStatement(connection, "migrate_3.7_eap_1_part2").execute();
                        databaseVersion = new Version("3.7-EAP-1");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.7-EAP-4
                    if (databaseVersion.compareTo(new Version("3.7-EAP-4")) < 0) {
                        LOG.info("Migrating database to 3.7 EAP 4.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.7_eap_4").execute();
                        databaseVersion = new Version("3.7-EAP-4");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.8.0-EAP-1
                    if (databaseVersion.compareTo(new Version("3.8.0-EAP-1")) < 0) {
                        LOG.info("Migrating database to 3.8.0 EAP 1.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_1").execute();
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_1_part_2").execute();
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_1_part_3").execute();
                        databaseVersion = new Version("3.8.0-EAP-1");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.8.0-EAP-5
                    if (databaseVersion.compareTo(new Version("3.8.0-EAP-5")) < 0) {
                        LOG.info("Migrating database to 3.8.0 EAP 5.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_5_part_1").execute();
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_5_part_2").execute();
                        databaseVersion = new Version("3.8.0-EAP-5");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.8.0-EAP-7
                    if (databaseVersion.compareTo(new Version("3.8.0-EAP-7")) < 0) {
                        LOG.info("Migrating database to 3.8.0 EAP 7.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_7").execute();
                        MyTunesRssUtils.createStatement(connection, "recreateHelpTablesAlbum").execute();
                        databaseVersion = new Version("3.8.0-EAP-7");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.8.0-EAP-9
                    if (databaseVersion.compareTo(new Version("3.8.0-EAP-9")) < 0) {
                        LOG.info("Migrating database to 3.8.0 EAP 9.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_9").execute();
                        databaseVersion = new Version("3.8.0-EAP-9");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 3.8.0-EAP-10
                    if (databaseVersion.compareTo(new Version("3.8.0-EAP-10")) < 0) {
                        LOG.info("Migrating database to 3.8.0 EAP 10.");
                        MyTunesRssUtils.createStatement(connection, "migrate_3.8.0_eap_10").execute();
                        databaseVersion = new Version("3.8.0-EAP-10");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 4.0.0-EAP-7
                    if (databaseVersion.compareTo(new Version("4.0.0-EAP-7")) < 0) {
                        LOG.info("Migrating database to 4.0.0-EAP-7.");
                        MyTunesRssUtils.createStatement(connection, "migrate_4.0.0_eap_7").execute();
                        databaseVersion = new Version("4.0.0-EAP-7");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 4.1.0-EAP-1
                    if (databaseVersion.compareTo(new Version("4.1.0-EAP-1")) < 0) {
                        LOG.info("Migrating database to 4.1.0-EAP-1.");
                        MyTunesRssUtils.createStatement(connection, "migrate_4.1.0_eap_1").execute();
                        MyTunesRssUtils.createStatement(connection, "updateStatistics").execute();
                        databaseVersion = new Version("4.1.0-EAP-1");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 4.1.0-EAP-4
                    if (databaseVersion.compareTo(new Version("4.1.0-EAP-4")) < 0) {
                        LOG.info("Migrating database to 4.1.0-EAP-4.");
                        MyTunesRssUtils.createStatement(connection, "migrate_4.1.0_eap_4").execute();
                        databaseVersion = new Version("4.1.0-EAP-4");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 4.1.0-EAP-6
                    if (databaseVersion.compareTo(new Version("4.1.0-EAP-6")) < 0) {
                        LOG.info("Migrating database to 4.1.0-EAP-6.");
                        MyTunesRssUtils.createStatement(connection, "migrate_4.1.0_eap_6").execute();
                        databaseVersion = new Version("4.1.0-EAP-6");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 4.2-RC-2
                    if (databaseVersion.compareTo(new Version("4.2-RC-2")) < 0) {
                        LOG.info("Migrating database to 4.2-RC-2.");
                        MyTunesRssUtils.createStatement(connection, "migrate_4.2-RC-2").execute();
                        databaseVersion = new Version("4.2-RC-2");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                    }
                    // migration for 4.3
                    if (databaseVersion.compareTo(new Version("4.3")) < 0) {
                        LOG.info("Migrating database to 4.3.");
                        MyTunesRssUtils.createStatement(connection, "migrate_4.3_part_1").execute();
                        MyTunesRssUtils.createStatement(connection, "migrate_4.3_part_2").execute();
                        databaseVersion = new Version("4.3");
                        new UpdateDatabaseVersionStatement(databaseVersion.toString()).execute(connection);
                        MyTunesRss.RUN_DATABASE_REFRESH_ON_STARTUP = true; // force a database refresh to insert the source ids!
                    }
                } finally {
                    connection.setAutoCommit(autoCommit);
                }
            }
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