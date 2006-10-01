/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class DeleteAllContentStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(DeleteAllContentStatement.class);

    public void execute(Connection connection) {
        deleteCatchException(connection, "playlist");
        deleteCatchException(connection, "album");
        deleteCatchException(connection, "artist");
        deleteCatchException(connection, "track");
        deleteCatchException(connection, "pager");
    }

    private void deleteCatchException(Connection connection, String tableName) {
        try {
            connection.createStatement().execute("DELETE FROM " + tableName);
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not delete contents from table \"" + tableName + "\".", e);
            }
        }
    }
}