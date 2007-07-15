/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class DropAllTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "dropAllTables").execute();
    }
}