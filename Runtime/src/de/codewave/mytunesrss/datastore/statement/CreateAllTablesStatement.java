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
        connection.createStatement().execute("SET DATABASE COLLATION \"Latin1_General\"");
        connection.createStatement().execute(
                "CREATE TABLE track ( id varchar, name VARCHAR, artist VARCHAR, album VARCHAR, time INTEGER, track_number INTEGER, file VARCHAR, UNIQUE ( id ) )");
        connection.createStatement().execute("CREATE TABLE playlist ( id VARCHAR, name VARCHAR, type VARCHAR, UNIQUE ( id ) )");
        connection.createStatement().execute("CREATE TABLE link_track_playlist ( track_id VARCHAR, playlist_id VARCHAR )");
        connection.createStatement().execute("CREATE TABLE itunes ( lastupdate BIGINT )");
        connection.createStatement().execute("INSERT INTO itunes VALUES ( 0 )");
        connection.createStatement().execute("CREATE TABLE album ( name VARCHAR, track_count INTEGER, artist_count INTEGER, artist VARCHAR )");
        connection.createStatement().execute("CREATE TABLE artist ( name VARCHAR, track_count INTEGER, album_count INTEGER )");
        connection.createStatement().execute("CREATE INDEX idx_track_id ON track ( id )");
        connection.createStatement().execute("CREATE INDEX idx_track_name ON track ( name )");
        connection.createStatement().execute("CREATE INDEX idx_track_artist ON track ( artist )");
        connection.createStatement().execute("CREATE INDEX idx_track_album ON track ( album )");
        connection.createStatement().execute("CREATE INDEX idx_link_track_playlist_playlist_id ON link_track_playlist ( playlist_id )");
        connection.createStatement().execute("CREATE INDEX idx_album_name ON album ( name )");
        connection.createStatement().execute("CREATE INDEX idx_album_artist ON album ( artist )");
        connection.createStatement().execute("CREATE INDEX idx_artist_name ON artist ( name )");
        connection.commit();
    }
}