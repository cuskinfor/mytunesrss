/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class CreateAllTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "preCreateAllTables").execute();
        MyTunesRssUtils.createStatement(connection, "createAllTables").execute();
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "initializeAllTables");
        statement.setString("version", MyTunesRss.VERSION);
        statement.execute();
    }
}