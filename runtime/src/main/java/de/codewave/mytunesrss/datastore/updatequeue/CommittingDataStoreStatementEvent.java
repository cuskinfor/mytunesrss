/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class CommittingDataStoreStatementEvent extends DataStoreStatementEvent {

    public CommittingDataStoreStatementEvent(DataStoreStatement statement, boolean checkpointRelevant) {
        super(statement, checkpointRelevant);
    }

    public CommittingDataStoreStatementEvent(DataStoreStatement statement, boolean checkpointRelevant, String exLogMsg) {
        super(statement, checkpointRelevant, exLogMsg);
    }

    public boolean execute(DataStoreSession session) {
        try {
            super.execute(session);
        } finally {
            session.commit();
        }
        return false;
    }
}
