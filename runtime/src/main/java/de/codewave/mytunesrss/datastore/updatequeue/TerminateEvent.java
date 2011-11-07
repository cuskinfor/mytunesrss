package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;

public class TerminateEvent implements DatabaseUpdateEvent {

    public boolean execute(DataStoreSession session) {
        session.commit();
        return false;
    }

    public boolean isTerminate() {
        return true;
    }

    public boolean isStartTransaction() {
        return false;
    }
}
