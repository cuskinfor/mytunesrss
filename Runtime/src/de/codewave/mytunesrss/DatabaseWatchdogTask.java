/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.task.*;
import de.codewave.mytunesrss.settings.*;

import java.util.*;
import java.net.*;
import java.sql.*;

import org.apache.commons.logging.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.DatabaseWatchdogTask
 */
public class DatabaseWatchdogTask extends TimerTask {
    private static final Log LOG = LogFactory.getLog(DatabaseWatchdogTask.class);

    private int myInterval;
    private URL myLibrary;
    private Options myOptionsForm;

    public DatabaseWatchdogTask(Options options, int interval, URL library) {
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
        MyTunesRss.DATABASE_WATCHDOG.schedule(new DatabaseWatchdogTask(myOptionsForm, myInterval, myLibrary), 1000 * myInterval);
    }
}