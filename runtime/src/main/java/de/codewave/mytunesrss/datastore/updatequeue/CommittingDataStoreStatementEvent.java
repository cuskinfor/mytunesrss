/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;

public class CommittingDataStoreStatementEvent extends DataStoreStatementEvent {

    public CommittingDataStoreStatementEvent(DataStoreStatement statement, boolean checkpointRelevant) {
        super(statement, checkpointRelevant);
    }

    public CommittingDataStoreStatementEvent(DataStoreStatement statement, boolean checkpointRelevant, String exLogMsg) {
        super(statement, checkpointRelevant, exLogMsg);
    }

    @Override
    public boolean execute(DataStoreSession session) {
        try {
            super.execute(session);
        } finally {
            session.commit();
        }
        return false;
    }
}
