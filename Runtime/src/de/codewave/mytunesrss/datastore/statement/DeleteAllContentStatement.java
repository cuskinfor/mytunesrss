/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class DeleteAllContentStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(DeleteAllContentStatement.class);

    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "deleteAllContent").execute(new SmartStatementExceptionHandler() {
            public void handleException(SQLException sqlException, boolean b) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not delete contents from table.", sqlException);
                }
            }
        });
    }
}