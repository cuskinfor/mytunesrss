package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;

public class MyTunesRssEventEvent implements DatabaseUpdateEvent {

    private MyTunesRssEvent myEvent;

    public MyTunesRssEventEvent(MyTunesRssEvent event) {
        myEvent = event;
    }

    @Override
    public boolean execute(DataStoreSession transaction) {
        MyTunesRss.LAST_DATABASE_EVENT.set(myEvent);
        MyTunesRssEventManager.getInstance().fireEvent(myEvent);
        return true;
    }

    @Override
    public boolean isCheckpointRelevant() {
        return false;
    }

    @Override
    public boolean isStartTransaction() {
        return false;
    }

    @Override
    public boolean isTerminate() {
        return false;
    }
}
