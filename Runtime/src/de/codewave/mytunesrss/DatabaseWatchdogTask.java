/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.task.*;

import java.util.*;
import java.net.*;
import java.sql.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.DatabaseWatchdogTask
 */
public class DatabaseWatchdogTask extends TimerTask {
    private static final Log LOG = LogFactory.getLog(DatabaseWatchdogTask.class);

    private int myInterval;
    private URL myLibrary;

    public DatabaseWatchdogTask(int interval, URL library) {
        myInterval = interval;
        myLibrary = library;
    }

    public void run() {
        try {
            if (DatabaseBuilderTask.needsUpdate(myLibrary)) {
                new DatabaseBuilderTask(myLibrary).execute();
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not automatically update database.", e);
            }
        }
        MyTunesRss.DATABASE_WATCHDOG.schedule(new DatabaseWatchdogTask(myInterval, myLibrary), 1000 * myInterval);
    }
}