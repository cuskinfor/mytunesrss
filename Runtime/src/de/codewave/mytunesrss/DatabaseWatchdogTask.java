/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
import org.apache.commons.logging.*;

import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.DatabaseWatchdogTask
 */
public class DatabaseWatchdogTask extends TimerTask {
    private static final Log LOG = LogFactory.getLog(DatabaseWatchdogTask.class);

    private int myInterval;
    private URL myLibrary;
    private Options myOptionsForm;
    private Timer myTimer;

    public DatabaseWatchdogTask(Timer timer, Options options, int interval, URL library) {
        myTimer = timer;
        myOptionsForm = options;
        myInterval = interval;
        myLibrary = library;
    }

    public void run() {
        try {
            if (DatabaseBuilderTask.needsUpdate(myLibrary)) {
                new DatabaseBuilderTask(myLibrary, myOptionsForm).execute();
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not automatically update database.", e);
            }
        }
        try {
            myTimer.schedule(new DatabaseWatchdogTask(myTimer, myOptionsForm, myInterval, myLibrary), 1000 * myInterval);
        } catch (IllegalStateException e) {
            // timer was cancelled, so we just don't schedule any further tasks
        }
    }
}