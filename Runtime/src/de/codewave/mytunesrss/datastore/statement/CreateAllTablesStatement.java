/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class CreateAllTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        connection.createStatement().execute("SET DATABASE COLLATION \"Latin1_General\"");
        connection.createStatement().execute(
                "CREATE CACHED TABLE track ( id varchar, name VARCHAR, artist VARCHAR, album VARCHAR, time INTEGER, track_number INTEGER, file VARCHAR, UNIQUE ( id ) )");
        connection.createStatement().execute("CREATE CACHED TABLE playlist ( id VARCHAR, name VARCHAR, type VARCHAR, UNIQUE ( id ) )");
        connection.createStatement().execute(
                "CREATE CACHED TABLE link_track_playlist ( index INTEGER, track_id VARCHAR, playlist_id VARCHAR, FOREIGN KEY (track_id) REFERENCES track (id) ON DELETE CASCADE, FOREIGN KEY (playlist_id) REFERENCES playlist (id) ON DELETE CASCADE )");
        connection.createStatement().execute("CREATE TABLE mytunesrss ( lastupdate BIGINT, version VARCHAR )");
        connection.createStatement().execute("INSERT INTO mytunesrss VALUES ( 0, '" + MyTunesRss.VERSION + "' )");
        connection.createStatement().execute(
                "CREATE CACHED TABLE album ( name VARCHAR, first VARCHAR, track_count INTEGER, artist_count INTEGER, artist VARCHAR, UNIQUE ( name ) )");
        connection.createStatement().execute(
                "CREATE CACHED TABLE artist ( name VARCHAR, first VARCHAR, track_count INTEGER, album_count INTEGER, UNIQUE ( name ) )");
        connection.createStatement().execute("CREATE TABLE pager ( index INTEGER, condition VARCHAR, value VARCHAR )");

        connection.createStatement().execute("CREATE INDEX idx_track_name ON track ( name )");
        connection.createStatement().execute("CREATE INDEX idx_track_artist ON track ( artist )");
        connection.createStatement().execute("CREATE INDEX idx_track_album ON track ( album )");
        connection.createStatement().execute("CREATE INDEX idx_link_track_playlist_playlist_id ON link_track_playlist ( playlist_id )");
        connection.createStatement().execute("CREATE INDEX idx_album_first ON album ( first )");
        connection.createStatement().execute("CREATE INDEX idx_album_artist ON album ( artist )");
        connection.createStatement().execute("CREATE INDEX idx_artist_first ON artist ( first )");

        connection.createStatement().execute("CREATE SEQUENCE mytunes_playlist_id");

        connection.createStatement().execute("SET PROPERTY \"hsqldb.cache_scale\" 12");
        connection.createStatement().execute("SET PROPERTY \"hsqldb.cache_size_scale\" 9");
    }
}