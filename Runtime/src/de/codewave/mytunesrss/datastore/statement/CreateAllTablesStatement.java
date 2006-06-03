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
                "CREATE CACHED TABLE track ( id VARCHAR(20) NOT NULL, name VARCHAR(255) NOT NULL, artist VARCHAR(255) NOT NULL, album VARCHAR(255) NOT NULL, time INTEGER, track_number INTEGER, file VARCHAR(1024) NOT NULL, UNIQUE ( id ) )");
        connection.createStatement().execute("CREATE CACHED TABLE playlist ( id VARCHAR(20) NOT NULL, name VARCHAR(255) NOT NULL, type VARCHAR(20) NOT NULL, track_count INTEGER, UNIQUE ( id ) )");
        connection.createStatement().execute(
                "CREATE CACHED TABLE link_track_playlist ( index INTEGER, track_id VARCHAR(20) NOT NULL, playlist_id VARCHAR(20) NOT NULL, FOREIGN KEY (track_id) REFERENCES track (id) ON DELETE CASCADE, FOREIGN KEY (playlist_id) REFERENCES playlist (id) ON DELETE CASCADE )");
        connection.createStatement().execute("CREATE CACHED TABLE system_information ( lastupdate BIGINT, version VARCHAR(20) NOT NULL, itunes_library_id VARCHAR(20) )");
        connection.createStatement().execute("INSERT INTO system_information VALUES ( null, '" + MyTunesRss.VERSION + "', null )");
        connection.createStatement().execute(
                "CREATE CACHED TABLE album ( name VARCHAR(255) NOT NULL, first_char VARCHAR(1), track_count INTEGER, artist_count INTEGER, artist VARCHAR(255), UNIQUE ( name ) )");
        connection.createStatement().execute(
                "CREATE CACHED TABLE artist ( name VARCHAR(255) NOT NULL, first_char VARCHAR(1), track_count INTEGER, album_count INTEGER, UNIQUE ( name ) )");
        connection.createStatement().execute("CREATE CACHED TABLE pager ( type VARCHAR(20) NOT NULL, index INTEGER NOT NULL, condition VARCHAR(255) NOT NULL, value VARCHAR(255) NOT NULL, content_count INTEGER NOT NULL, UNIQUE ( type, index ) )");

        connection.createStatement().execute("CREATE INDEX idx_track_name ON track ( name )");
        connection.createStatement().execute("CREATE INDEX idx_track_artist ON track ( artist )");
        connection.createStatement().execute("CREATE INDEX idx_track_album ON track ( album )");
        connection.createStatement().execute("CREATE INDEX idx_link_track_playlist_playlist_id ON link_track_playlist ( playlist_id )");
        connection.createStatement().execute("CREATE INDEX idx_album_first ON album ( first_char )");
        connection.createStatement().execute("CREATE INDEX idx_album_artist ON album ( artist )");
        connection.createStatement().execute("CREATE INDEX idx_artist_first ON artist ( first_char )");
        connection.createStatement().execute("CREATE INDEX idx_playlist_id ON playlist ( id )");

        connection.createStatement().execute("CREATE SEQUENCE playlist_id_sequence");
    }
}