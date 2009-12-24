/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.Task;
import de.codewave.utils.swing.TaskExecutor;
import de.codewave.utils.swing.TaskFinishedListener;

import java.io.IOException;
import java.sql.SQLException;

public class RefreshSmartPlaylistsAndLuceneIndexTask extends Task {

    private String[] trackIds;

    public static void execute(String[] trackIds) {
        TaskExecutor.execute(new RefreshSmartPlaylistsAndLuceneIndexTask(trackIds), new TaskFinishedListener() {
            public void taskFinished(Task task) {
                // intentionally left blank
            }
        });
    }

    public RefreshSmartPlaylistsAndLuceneIndexTask(String[] trackIds) {
        this.trackIds = trackIds;
    }

    @Override
    public void execute() throws IOException, SQLException {
        MyTunesRss.LUCENE_TRACK_SERVICE.updateTracks(trackIds);
        DataStoreSession transaction = MyTunesRss.STORE.getTransaction();
        transaction.executeStatement(new RefreshSmartPlaylistsStatement());
        transaction.commit();
    }
}
