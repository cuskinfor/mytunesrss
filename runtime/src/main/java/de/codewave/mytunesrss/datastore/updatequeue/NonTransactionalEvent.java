package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;

public interface NonTransactionalEvent extends DatabaseUpdateEvent {
    void execute();
}
