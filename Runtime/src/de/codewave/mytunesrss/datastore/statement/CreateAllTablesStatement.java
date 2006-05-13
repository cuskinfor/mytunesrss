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
                "CREATE TABLE track ( id varchar, name varchar_ignorecase, artist varchar_ignorecase, album varchar_ignorecase, time integer, track_number integer, file varchar )");
        connection.createStatement().execute("CREATE TABLE playlist ( id varchar, name varchar_ignorecase )");
        connection.createStatement().execute("CREATE TABLE link_track_playlist ( track_id varchar, playlist_id varchar )");
        connection.createStatement().execute("CREATE TABLE itunes ( lastupdate BIGINT )");
        connection.createStatement().execute("INSERT INTO itunes VALUES ( 0 )");
        connection.commit();
    }
}