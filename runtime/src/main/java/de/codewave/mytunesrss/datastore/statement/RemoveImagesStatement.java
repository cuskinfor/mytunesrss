/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
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
public class RemoveImagesStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveImagesStatement.class);

    private Collection<String> myDataSourceIds;

    public RemoveImagesStatement(Collection<String> dataSourceIds) {
        myDataSourceIds = dataSourceIds;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeImagesForDataSources");
        statement.setItems("source_id", myDataSourceIds);
        statement.execute();
    }
}