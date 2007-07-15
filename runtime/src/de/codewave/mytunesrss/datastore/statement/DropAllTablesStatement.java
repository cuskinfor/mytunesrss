/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class DropAllTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "dropAllTables").execute();
    }
}