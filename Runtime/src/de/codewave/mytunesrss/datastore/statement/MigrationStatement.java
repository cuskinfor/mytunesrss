/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
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
        if (version.compareTo("2.0.1") < 0) {
            // update to 2.0.1
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
        if (version.compareTo("2.1") < 0) {
            // update to 2.1
            if (LOG.isInfoEnabled()) {
                LOG.info("Migrating database to version 2.1");
            }
            connection.createStatement().execute("ALTER TABLE track ADD COLUMN protected BOOLEAN");
            connection.createStatement().execute("ALTER TABLE track ADD COLUMN video BOOLEAN");
            connection.createStatement().execute("UPDATE system_information SET lastupdate = 0");
            connection.createStatement().execute("UPDATE system_information SET version = '2.1'");
            version = "2.1";
        }
        if (version.compareTo("2.3") < 0) {
            // update to 2.3
            if (LOG.isInfoEnabled()) {
                LOG.info("Migrating database to version 2.3");
            }
            connection.createStatement().execute("DROP TABLE link_track_playlist");
            connection.createStatement().execute("ALTER TABLE track ALTER COLUMN id VARCHAR(2000)");
            connection.createStatement().execute("ALTER TABLE track ADD COLUMN source VARCHAR(20)");
            connection.createStatement().execute(
                    "CREATE CACHED TABLE link_track_playlist ( index INTEGER, track_id VARCHAR(20) NOT NULL, playlist_id VARCHAR(20) NOT NULL, CONSTRAINT fk_linktrackplaylist_trackid FOREIGN KEY (track_id) REFERENCES track (id) ON DELETE CASCADE, CONSTRAINT fk_linktrackplaylist_playlistid FOREIGN KEY (playlist_id) REFERENCES playlist (id) ON DELETE CASCADE )");
            connection.createStatement().execute("ALTER TABLE system_information ADD COLUMN basedir_id VARCHAR(2000)");
            connection.createStatement().execute("UPDATE track SET source = '" + TrackSource.ITunes.name() + "'");
            connection.createStatement().execute("UPDATE system_information SET version = '2.3'");
            version = "2.3";
        }
        if (version.compareTo("3.0") < 0) {
            // update to 3.0
            if (LOG.isInfoEnabled()) {
                LOG.info("Migrating database to version 3.0");
            }
            connection.createStatement().execute("ALTER TABLE system_information DROP COLUMN basedir_id");
            connection.createStatement().execute("ALTER TABLE link_track_playlist ALTER COLUMN track_id VARCHAR(2000) NOT NULL");
            connection.createStatement().execute("ALTER TABLE track ADD COLUMN genre VARCHAR(255)");
            connection.createStatement().execute("ALTER TABLE artist ALTER COLUMN name VARCHAR_IGNORECASE(255)");
            connection.createStatement().execute("ALTER TABLE album ALTER COLUMN name VARCHAR_IGNORECASE(255)");
            connection.createStatement().execute("ALTER TABLE track ALTER COLUMN artist VARCHAR_IGNORECASE(255)");
            connection.createStatement().execute("ALTER TABLE track ALTER COLUMN album VARCHAR_IGNORECASE(255)");
            version = "3.0";
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