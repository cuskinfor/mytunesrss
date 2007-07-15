/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.logging.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.DatabaseWatchdogTask
 */
public class DatabaseWatchdogTask extends TimerTask {
    private static final Log LOG = LogFactory.getLog(DatabaseWatchdogTask.class);

    private int myInterval;
    private Timer myTimer;

    public DatabaseWatchdogTask(Timer timer, int interval) {
        myTimer = timer;
        myInterval = interval;
    }

    public void run() {
        try {
            if (MyTunesRss.createDatabaseBuilderTask().needsUpdate()) {
                MyTunesRss.createDatabaseBuilderTask().execute();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not automatically update database.", e);
            }
        }
        try {
            myTimer.schedule(new DatabaseWatchdogTask(myTimer, myInterval), myInterval * 60000);
        } catch (IllegalStateException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not re-schedule task!", e);
            }
        }
    }
}