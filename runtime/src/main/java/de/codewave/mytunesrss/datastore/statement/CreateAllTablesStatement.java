/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class CreateAllTablesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAllTablesStatement.class);

    public void execute(Connection connection) throws SQLException {
        LOGGER.debug("Creating all tables. Executing pre-create statement.");
        MyTunesRssUtils.createStatement(connection, "preCreateAllTables").execute();
        LOGGER.debug("Executing create statement.");
        MyTunesRssUtils.createStatement(connection, "createAllTables").execute();
        LOGGER.debug("Initializing tables with some default values (using version \"" + MyTunesRss.VERSION + "\").");
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "initializeAllTables");
        statement.setString("version", MyTunesRss.VERSION);
        statement.execute();
    }
}