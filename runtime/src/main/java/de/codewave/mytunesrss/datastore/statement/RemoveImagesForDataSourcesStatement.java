/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.RemoveTrackStatement
 */
public class RemoveImagesForDataSourcesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveImagesForDataSourcesStatement.class);

    private Collection<String> myDataSourceIds;

    public RemoveImagesForDataSourcesStatement(Collection<String> dataSourceIds) {
        myDataSourceIds = dataSourceIds;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeImagesForDataSources");
        statement.setItems("source_id", myDataSourceIds);
        statement.execute();
    }
}
