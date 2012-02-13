/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

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
    public void run() {
        removeOldTempPlaylists();
        removePlaylistsWithoutUser();
    }

    private void removePlaylistsWithoutUser() {
        final Set<String> userNames = new HashSet<String>();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            userNames.add(user.getName());
        }
        MyTunesRss.STORE.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "deleteOrphanedPlaylists");
                statement.setItems("existingUsers", userNames.toArray(new String[userNames.size()]));
                statement.execute();
            }
        });
    }

    private void removeOldTempPlaylists() {
        MyTunesRss.STORE.executeStatement(new RemoveOldTempPlaylistsStatement());
    }
}
