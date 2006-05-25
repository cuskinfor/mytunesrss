/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class DropAllTablesStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(DropAllTablesStatement.class);

    public void execute(Connection connection) {
        executeCatchException(connection, "DROP TABLE link_track_playlist");
        executeCatchException(connection, "DROP TABLE playlist");
        executeCatchException(connection, "DROP TABLE album");
        executeCatchException(connection, "DROP TABLE artist");
        executeCatchException(connection, "DROP TABLE track");
        executeCatchException(connection, "DROP TABLE pager");
        executeCatchException(connection, "DROP TABLE mytunesrss");

        executeCatchException(connection, "DROP SEQUENCE mytunes_playlist_id");

        executeCatchException(connection, "DROP INDEX idx_track_name IF EXISTS");
        executeCatchException(connection, "DROP INDEX idx_track_artist IF EXISTS");
        executeCatchException(connection, "DROP INDEX idx_track_album IF EXISTS");
        executeCatchException(connection, "DROP INDEX idx_link_track_playlist_playlist_id IF EXISTS");
        executeCatchException(connection, "DROP INDEX idx_album_first IF EXISTS");
        executeCatchException(connection, "DROP INDEX idx_album_artist IF EXISTS");
        executeCatchException(connection, "DROP INDEX idx_artist_first IF EXISTS");
    }

    private void executeCatchException(Connection connection, String statement) {
        try {
            connection.createStatement().execute(statement);
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not drop table.", e);
            }
        }
    }
}