/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdatePhotoStatement
 */
public class UpdatePhotoStatement extends InsertOrUpdatePhotoStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UpdatePhotoStatement.class);

    public UpdatePhotoStatement(String sourceId) {
        super(sourceId);
    }

    @Override
    protected void logError(String id, SQLException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(String.format("Could not update photo with ID \"%s\" in database.", id), e);
        }
    }

    @Override
    protected String getStatementName() {
        return "updatePhoto";
    }


}