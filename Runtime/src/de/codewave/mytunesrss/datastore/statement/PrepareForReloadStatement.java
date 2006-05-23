/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.PrepareForReloadStatement
 */
public class PrepareForReloadStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        connection.createStatement().execute("DELETE FROM track");
        connection.createStatement().execute("DELETE FROM playlist WHERE type = '" + PlaylistType.ITunes + "' ");
        connection.createStatement()
                .execute("DELETE FROM link_track_playlist WHERE NOT EXISTS ( SELECT * FROM playlist p WHERE p.id = playlist_id )");
        connection.createStatement().execute("DELETE FROM album");
        connection.createStatement().execute("DELETE FROM artist");
    }
}