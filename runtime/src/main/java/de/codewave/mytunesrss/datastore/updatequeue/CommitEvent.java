package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;

public class CommitEvent implements TransactionalEvent {
    public DataStoreSession execute(DataStoreSession session) {
        session.commit();
        return null;
    }

    public boolean isIgnoreWithoutTransaction() {
        return true;
    }
}
