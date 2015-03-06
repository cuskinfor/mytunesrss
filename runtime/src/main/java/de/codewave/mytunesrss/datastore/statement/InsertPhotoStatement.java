/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertPhotoStatement
 */
public class InsertPhotoStatement extends InsertOrUpdatePhotoStatement {
    private static final Logger LOG = LoggerFactory.getLogger(InsertPhotoStatement.class);

    public InsertPhotoStatement(String sourceId) {
        super(sourceId);
    }

    @Override
    protected void logError(String id, SQLException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(String.format("Could not insert photo with ID \"%s\" into database.", id), e);
        }
    }

    @Override
    protected String getStatementName() {
        return "insertPhoto";
    }

}