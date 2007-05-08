/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import org.apache.commons.logging.*;
import de.codewave.mytunesrss.datastore.itunes.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.xml.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.ItunesLoaderr
 */
public class DataLoader {
    private static final Log LOG = LogFactory.getLog(DataLoader.class);

    protected static void removeObsoleteTracks(DataStoreSession storeSession, Set<String> databaseIds) throws SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Removing " + databaseIds.size() + " obsolete iTunes tracks.");
        }
        int count = 0;
        DeleteTrackStatement statement = new DeleteTrackStatement();
        for (String id : databaseIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            count++;
            if (count == 5000) {
                count = 0;
                storeSession.commitAndContinue();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Committing transaction after 5000 deleted tracks.");
                }
            }
        }
    }
}