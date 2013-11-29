/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class CheckpointEvent implements DatabaseUpdateEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckpointEvent.class);

    public boolean execute(DataStoreSession session) {
        session.commit();
        try {
            session.executeStatement(new UpdateStatisticsStatement());
            SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
            LOGGER.info("System information: " + systemInformation + ".");
            session.commit();
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        return false;
    }

    protected void refreshAccessories(DataStoreSession session) {
        session.commit();
        try {
            StopWatch.start("Recreating help tables");
            session.executeStatement(new RecreateHelpTablesStatement(true, true, true));
            StopWatch.stop();
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        try {
            StopWatch.start("Refreshing smart playlists");
            session.executeStatement(new RefreshSmartPlaylistsStatement());
            StopWatch.stop();
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        try {
            StopWatch.start("Updating statistics");
            session.executeStatement(new UpdateStatisticsStatement());
            StopWatch.stop();
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        try {
            StopWatch.start("Updating user database references");
            MyTunesRssUtils.updateUserDatabaseReferences(session);
            StopWatch.stop();
        } catch (SQLException e) {
            LOGGER.warn("Could not update user database references.", e);
        } finally {
            session.commit();
        }
        try {
            SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
            LOGGER.info("System information: " + systemInformation + ".");
            session.commit();
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
    }

    public boolean isCheckpointRelevant() {
        return false;
    }

    public boolean isTerminate() {
        return false;
    }

    public boolean isStartTransaction() {
        return true;
    }
}
