/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.statistics.GetStatisticsEventsQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            long start = System.currentTimeMillis();
            session.executeStatement(new RecreateHelpTablesStatement());
            LOGGER.info("Recreating help tables took " + (System.currentTimeMillis() - start) + " milliseconds.");
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        try {
            long start = System.currentTimeMillis();
            session.executeStatement(new RefreshSmartPlaylistsStatement());
            LOGGER.info("Refreshing smart playlists took " + (System.currentTimeMillis() - start) + " milliseconds.");
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        try {
            long start = System.currentTimeMillis();
            session.executeStatement(new UpdateStatisticsStatement());
            LOGGER.info("Updating statistics took " + (System.currentTimeMillis() - start) + " milliseconds.");
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        } finally {
            session.commit();
        }
        try {
            long start = System.currentTimeMillis();
            MyTunesRssUtils.updateUserDatabaseReferences(session);
            LOGGER.info("Updating user database references took " + (System.currentTimeMillis() - start) + " milliseconds.");
        } catch (SQLException e) {
            LOGGER.warn("Could not update user database references.", e);
        } finally {
            session.commit();
        }
        try {
            MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        } catch (IOException e) {
            LOGGER.warn("Could not rebuild track index.", e);
        } catch (SQLException e) {
            LOGGER.warn("Could not rebuild track index.", e);
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
