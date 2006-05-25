/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.datastore.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.DeleteTrackStatement
 */
public class DeleteTrackStatement implements DataStoreStatement {
    private static final String SQL = "DELETE FROM track WHERE id = ?";

    private PreparedStatement myStatement;
    private String myId;

    public DeleteTrackStatement() {
        // intentionally left blank
    }

    public DeleteTrackStatement(DataStoreSession session) throws SQLException {
        myStatement = session.prepare(SQL);
    }

    public void setId(String id) {
        myId = id;
    }

    public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = myStatement != null ? myStatement : connection.prepareStatement(SQL);
        statement.setString(1, myId);
        statement.execute();
    }
}