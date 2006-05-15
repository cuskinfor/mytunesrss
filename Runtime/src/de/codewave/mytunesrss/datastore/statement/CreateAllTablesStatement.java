/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class CreateAllTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        connection.createStatement().execute(
                "CREATE TABLE track ( id varchar, name VARCHAR_IGNORECASE, artist VARCHAR_IGNORECASE, album VARCHAR_IGNORECASE, time INTEGER, track_number INTEGER, file VARCHAR )");
        connection.createStatement().execute("CREATE TABLE playlist ( id VARCHAR, name VARCHAR_IGNORECASE )");
        connection.createStatement().execute("CREATE TABLE link_track_playlist ( track_id VARCHAR, playlist_id VARCHAR )");
        connection.createStatement().execute("CREATE TABLE itunes ( lastupdate BIGINT )");
        connection.createStatement().execute("INSERT INTO itunes VALUES ( 0 )");
        connection.createStatement().execute("CREATE TABLE album ( name VARCHAR_IGNORECASE, track_count INTEGER, artist VARCHAR_IGNORECASE, various BOOLEAN)");
        connection.createStatement().execute("CREATE TABLE artist ( name VARCHAR_IGNORECASE, track_count INTEGER, album_count INTEGER )");
        connection.commit();
    }
}