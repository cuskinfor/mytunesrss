/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateTrackStatement
 */
public class UpdateTrackStatement extends InsertOrUpdateTrackStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateTrackStatement.class);

    public UpdateTrackStatement(TrackSource source) {
        super(source);
    }

    @Override
    protected void logError(String id, SQLException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(String.format("Could not update track with ID \"%s\" in database.", id), e);
        }
    }

    @Override
    protected String getStatementName() {
        return "updateTrack";
    }


}