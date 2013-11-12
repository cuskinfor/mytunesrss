/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class DropAllTablesStatement implements DataStoreStatement {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DropAllTablesStatement.class);
    
    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "dropAllTables").execute();
        try {
            MyTunesRss.LUCENE_TRACK_SERVICE.deleteLuceneIndex();
        } catch (IOException e) {
            LOGGER.error("Could not delete lucene index.", e);
        }
    }
}
