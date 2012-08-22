/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * The maintenance runnable performs the following jobs:
 *
 * - Delete orphaned playlists (with no existing owner user).
 * - Delete old temporary playlists.
 */
public class MaintenanceRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceRunnable.class);

    public void run() {
        try {
            LOGGER.debug("Starting maintenance job.");
            removeOldTempPlaylists();
            removePlaylistsWithoutUser();
        } catch (RuntimeException e) {
            LOGGER.warn("Encountered unexpected exception. Caught to keep scheduled task alive.", e);
        }
    }

    private void removePlaylistsWithoutUser() {
        LOGGER.debug("Maintenance job: removing orphaned playlist.");
        final Set<String> userNames = new HashSet<String>();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            userNames.add(user.getName());
        }
        try {
            MyTunesRss.STORE.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "deleteOrphanedPlaylists");
                    statement.setItems("existingUsers", userNames.toArray(new String[userNames.size()]));
                    statement.execute();
                }
            });
        } catch (SQLException e) {
            LOGGER.warn("Could not remove orphaned playlists.", e);
        }
    }

    private void removeOldTempPlaylists() {
        LOGGER.debug("Maintenance job: removing old temporary playlists.");
        try {
            MyTunesRss.STORE.executeStatement(new RemoveOldTempPlaylistsStatement());
        } catch (SQLException e) {
            LOGGER.warn("Could not remove old temporary playlists.", e);
        }
    }
}
