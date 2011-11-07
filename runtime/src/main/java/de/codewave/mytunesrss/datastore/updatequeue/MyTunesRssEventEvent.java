package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;

public class MyTunesRssEventEvent implements NonTransactionalEvent {

    private MyTunesRssEvent myEvent;

    public MyTunesRssEventEvent(MyTunesRssEvent event) {
        myEvent = event;
    }

    public void execute() {
        MyTunesRss.LAST_DATABASE_EVENT = myEvent;
        MyTunesRssEventManager.getInstance().fireEvent(myEvent);
    }
}
