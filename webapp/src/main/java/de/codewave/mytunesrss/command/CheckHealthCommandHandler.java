/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.server.CheckHealthResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.CheckHealthCommandHandler
 */
public class CheckHealthCommandHandler extends MyTunesRssCommandHandler {
    private static final Log LOG = LogFactory.getLog(CheckHealthCommandHandler.class);

    public void execute() throws SQLException, IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Health check servlet called.");
        }
        MyTunesRssDataStore dataStore = getDataStore();
        if (dataStore == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Data store is null!");
            }
            getResponse().getOutputStream().write(CheckHealthResult.NULL_DATA_STORE);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Data store is up and running.");
            }
            getResponse().getOutputStream().write(CheckHealthResult.OK);
        }
    }
}