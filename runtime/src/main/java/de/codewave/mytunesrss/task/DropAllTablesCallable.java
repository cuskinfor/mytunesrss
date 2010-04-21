/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.DropAllTablesStatement;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseCallable
 */
public class DropAllTablesCallable implements Callable<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropAllTablesCallable.class);

    public Void call() throws SQLException {
        LOGGER.debug("Dropping all tables.");
        try {
            DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
            storeSession.executeStatement(new DropAllTablesStatement());
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        } catch (SQLException e) {
            LOGGER.error("Could not drop all tables.", e);
            if (MyTunesRss.CONFIG.isDefaultDatabase()) {
                MyTunesRss.CONFIG.setDeleteDatabaseOnExit(true);
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.shutdownAndDeleteDatabase"));
                MyTunesRssUtils.shutdownGracefully();
            } else {
                throw e;
            }
        }
        return null;
    }

}