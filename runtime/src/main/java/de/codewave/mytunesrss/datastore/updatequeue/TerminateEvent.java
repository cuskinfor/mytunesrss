package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class TerminateEvent implements DatabaseUpdateEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateEvent.class);

    public boolean execute(DataStoreSession session) {
        try {
            session.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    MyTunesRssUtils.createStatement(connection, "finishDatabaseUpdate").execute();
                }
            });
            session.executeStatement(new RefreshSmartPlaylistsStatement());
        } catch (SQLException e) {
            LOGGER.warn("Could not execute data store statement.", e);
            session.setRollbackOnly();
        }
        session.commit();
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.buildingTrackIndex");
        MyTunesRss.LAST_DATABASE_EVENT = event;
        MyTunesRssEventManager.getInstance().fireEvent(event);
        try {
            MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        } catch (IOException e) {
            LOGGER.warn("Could not rebuild track index.", e);
        } catch (SQLException e) {
            LOGGER.warn("Could not rebuild track index.", e);
        }
        return false;
    }

    public boolean isTerminate() {
        return true;
    }

    public boolean isStartTransaction() {
        return true;
    }
}
