/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.MaintenanceStatement;
import de.codewave.mytunesrss.datastore.statement.RecreateHelpTablesStatement;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.mytunesrss.datastore.statement.UpdateStatisticsStatement;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

public class MaintenanceEvent implements DatabaseUpdateEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceEvent.class);

    public boolean execute(DataStoreSession session) {
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseMaintenance");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        MyTunesRssEvent currentEvent = MyTunesRss.LAST_DATABASE_EVENT.getAndSet(event);
        try {
            try {
                session.commit();
                session.executeStatement(new MaintenanceStatement());
                session.commit();
            } catch (SQLException e) {
                LOGGER.warn("Could not execute data store statement.", e);
                MyTunesRssEventManager.getInstance().fireEvent(currentEvent);
                MyTunesRss.LAST_DATABASE_EVENT.set(currentEvent);
            }
        } finally {
            session.commit(); // make sure we have a proper state
        }
        return false;
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
