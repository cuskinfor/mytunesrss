/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.updatequeue.CommitEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.MyTunesRssEventEvent;
import de.codewave.mytunesrss.datastore.updatequeue.TerminateEvent;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ForcedImageUpdateCallable extends DatabaseBuilderCallable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForcedImageUpdateCallable.class);

    public ForcedImageUpdateCallable(boolean ignoreTimestamps) {
        super(ignoreTimestamps);
    }

    @Override
    public Boolean call() throws Exception {
        try {
            myQueue.offer(new MyTunesRssEventEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateInvalidatingImages")));
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Invalidating all existing images.");
            }
            resetLastImageUpdateForAllTracks();
            myQueue.offer(new CommitEvent());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting forced image update.");
            }
            runImageUpdate(System.currentTimeMillis());
            myQueue.offer(new CommitEvent());
            updateHelpTables(myQueue, 0); // update image references for albums
            myQueue.offer(new CommitEvent());
            deleteOrphanedImages();
            myQueue.offer(new CommitEvent());
        } finally {
            myQueue.offer(new MyTunesRssEventEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED)));
            myQueue.offer(new TerminateEvent() { });
        }
        return true;
    }

    private void resetLastImageUpdateForAllTracks() {
        myQueue.offer(new DataStoreStatementEvent(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                MyTunesRssUtils.createStatement(connection, "resetLastImageUpdateForAllTracks").execute();
            }
        }));
    }
}
