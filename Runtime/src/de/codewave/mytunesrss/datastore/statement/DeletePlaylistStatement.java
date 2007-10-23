/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.DeleteTrackStatement
 */
public class DeletePlaylistStatement implements DataStoreStatement {
    private static final String SQL = "DELETE FROM playlist WHERE id = ?";

    private PreparedStatement myStatement;
    private String myId;

    public DeletePlaylistStatement() {
        // intentionally left blank
    }

    public DeletePlaylistStatement(DataStoreSession session) throws SQLException {
        myStatement = session.prepare(DeletePlaylistStatement.SQL);
    }

    public void setId(String id) {
        myId = id;
    }

    public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = myStatement != null ? myStatement : connection.prepareStatement(DeletePlaylistStatement.SQL);
        statement.setString(1, myId);
        statement.execute();
    }
}