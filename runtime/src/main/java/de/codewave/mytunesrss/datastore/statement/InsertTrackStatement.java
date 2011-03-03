/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackStatement
 */
public class InsertTrackStatement extends InsertOrUpdateTrackStatement {
    private static final Logger LOG = LoggerFactory.getLogger(InsertTrackStatement.class);

    public InsertTrackStatement(TrackSource source) {
        super(source);
    }

    @Override
    protected void logError(String id, SQLException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(String.format("Could not insert track with ID \"%s\" into database.", id), e);
        }
    }

    protected String getStatementName() {
        return "insertTrack";
    }

}