/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.command.CheckHealthCommandHandler
 */
public class CheckHealthCommandHandler extends MyTunesRssCommandHandler {
    private static final Log LOG = LogFactory.getLog(CheckHealthCommandHandler.class);

    public void execute() throws SQLException, IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Health check servlet called.");
        }
        DataStore dataStore = getDataStore();
        if (dataStore == null || dataStore.executeQuery(FindTrackQuery.getForSearchTerm("%")).isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Data store is null or empty!");
            }
            getResponse().getOutputStream().write(CheckHealthResult.EMPTY_LIBRARY);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Data store is up and running.");
            }
            getResponse().getOutputStream().write(CheckHealthResult.OK);
        }
    }
}