/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.DropAllTablesStatement;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class DropAllTablesTask extends MyTunesRssTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropAllTablesTask.class);

    public void execute() throws SQLException {
        LOGGER.debug("Dropping all tables.");
        try {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.executeStatement(new DropAllTablesStatement());
            DatabaseBuilderTask.doCheckpoint(storeSession, true);
        } catch (SQLException e) {
            LOGGER.error("Could not drop all tables.", e);
            if (MyTunesRss.CONFIG.isDefaultDatabase()) {
                MyTunesRss.CONFIG.setDeleteDatabaseOnExit(true);
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.shutdownAndDeleteDatabase"));
                MyTunesRssUtils.shutdownGracefully();
            } else {
                throw e;
            }
        }
    }

}