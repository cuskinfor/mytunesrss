package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;

public class RollbackEvent implements TransactionalEvent {
    public DataStoreSession execute(DataStoreSession transaction) {
        transaction.rollback();
        return null;
    }

    public boolean isIgnoreWithoutTransaction() {
        return true;
    }
}
