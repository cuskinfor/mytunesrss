/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
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
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunning");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        MyTunesRss.LAST_DATABASE_EVENT = event;
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Removing all existing images.");
            }
            resetLastImageUpdateForAllTracks(storeSession);
            doCheckpoint(storeSession, true);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting forced image update.");
            }
            runImageUpdate(storeSession, System.currentTimeMillis());
            storeSession.commit();
            return true;
        } catch (Exception e) {
            storeSession.rollback();
            throw e;
        } finally {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        }
    }

    private void resetLastImageUpdateForAllTracks(DataStoreSession storeSession) throws SQLException {
        storeSession.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                MyTunesRssUtils.createStatement(connection, "resetLastImageUpdateForAllTracks").execute();
            }
        });

    }
}
