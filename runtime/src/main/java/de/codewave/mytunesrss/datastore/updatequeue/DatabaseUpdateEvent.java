package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;

public interface DatabaseUpdateEvent {
    boolean isTerminate();
    boolean isStartTransaction();
    boolean execute(DataStoreSession transaction);
    boolean isCheckpointRelevant();
}
