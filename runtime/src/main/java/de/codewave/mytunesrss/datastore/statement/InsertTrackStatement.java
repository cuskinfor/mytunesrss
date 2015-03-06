/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.lucene.AddLuceneTrack;
import de.codewave.mytunesrss.lucene.LuceneTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertTrackStatement
 */
public class InsertTrackStatement extends InsertOrUpdateTrackStatement {
    private static final Logger LOG = LoggerFactory.getLogger(InsertTrackStatement.class);

    public InsertTrackStatement(TrackSource source, String sourceId) {
        super(source, sourceId);
    }

    @Override
    protected LuceneTrack newLuceneTrack() {
        return new AddLuceneTrack(); 
    }

    @Override
    protected void logError(String id, SQLException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(String.format("Could not insert track with ID \"%s\" into database.", id), e);
        }
    }

    @Override
    protected String getStatementName() {
        return "insertTrack";
    }

}
