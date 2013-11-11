package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class TerminateEvent extends CheckpointEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateEvent.class);

    public boolean execute(DataStoreSession session) {
        try {
            try {
                session.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        MyTunesRssUtils.createStatement(connection, "removeOrphanedImages").execute();
                    }
                });
            } catch (SQLException e) {
                LOGGER.warn("Could not execute data store statement.", e);
            }
            refreshAccessories(session);
            super.execute(session);
            session.commit();
        } finally {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED);
            MyTunesRss.LAST_DATABASE_EVENT.set(event);
            MyTunesRssEventManager.getInstance().fireEvent(event);
        }
        return false;
    }

    public boolean isTerminate() {
        return true;
    }
}
