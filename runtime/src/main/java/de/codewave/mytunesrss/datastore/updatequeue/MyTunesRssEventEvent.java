package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;

public class MyTunesRssEventEvent implements DatabaseUpdateEvent {

    private MyTunesRssEvent myEvent;

    public MyTunesRssEventEvent(MyTunesRssEvent event) {
        myEvent = event;
    }

    public boolean execute(DataStoreSession transaction) {
        MyTunesRss.LAST_DATABASE_EVENT = myEvent;
        MyTunesRssEventManager.getInstance().fireEvent(myEvent);
        return true;
    }

    public boolean isStartTransaction() {
        return false;
    }

    public boolean isTerminate() {
        return false;
    }
}
