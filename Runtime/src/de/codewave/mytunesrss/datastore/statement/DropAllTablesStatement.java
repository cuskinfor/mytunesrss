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
    }

    private void executeCatchException(Connection connection, String statement) {
        try {
            connection.createStatement().execute(statement);
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not drop database object (statement: \"" + statement + "\").");
            }
        }
    }
}