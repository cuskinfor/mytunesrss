package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TerminateEvent extends CheckpointEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateEvent.class);

    public boolean execute(DataStoreSession session) {
        try {
            refreshAccessories(session);
            super.execute(session);
            session.commit();
        } finally {
            MyTunesRss.LUCENE_TRACK_SERVICE.flushTrackBuffer();
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
