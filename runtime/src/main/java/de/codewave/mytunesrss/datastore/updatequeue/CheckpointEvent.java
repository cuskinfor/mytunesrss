/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.RecreateHelpTablesStatement;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class CheckpointEvent implements DatabaseUpdateEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckpointEvent.class);

    public boolean execute(DataStoreSession session) {
        try {
            session.executeStatement(new RecreateHelpTablesStatement());
            session.executeStatement(new RefreshSmartPlaylistsStatement());
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
        }
        try {
            MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        } catch (IOException e) {
            LOGGER.warn("Could not rebuild track index.", e);
        } catch (SQLException e) {
            LOGGER.warn("Could not rebuild track index.", e);
        }
        return true;
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
