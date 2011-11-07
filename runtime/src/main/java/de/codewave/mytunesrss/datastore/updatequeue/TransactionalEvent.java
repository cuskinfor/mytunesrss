package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;

public interface TransactionalEvent extends DatabaseUpdateEvent {
    DataStoreSession execute(DataStoreSession transaction);
    boolean isIgnoreWithoutTransaction();
}
