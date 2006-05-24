/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.DatabaseBuilderTask
 */
public class DatabaseBuilderTask extends PleaseWait.Task {
    private static final Log LOG = LogFactory.getLog(DatabaseBuilderTask.class);

    public static boolean needsCreation() {
        try {
            return MyTunesRss.STORE.executeQuery(new DataStoreQuery<Boolean>() {
                public Collection<Boolean> execute(Connection connection) throws SQLException {
                    connection.createStatement().executeQuery("SELECT COUNT(*) FROM track");
                    return Collections.singletonList(Boolean.FALSE);
                }
            }).iterator().next();
        } catch (SQLException e) {
            return true;
        }
    }

    public static boolean needsUpdate(URL libraryXmlUrl) throws SQLException {
        if (libraryXmlUrl != null) {
            if (needsCreation()) {
                return true;
            }
            Collection<Long> lastUpdate = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Long>() {
                public Collection<Long> execute(Connection connection) throws SQLException {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT lastupdate FROM mytunes");
                    if (resultSet.next()) {
                        return Collections.singletonList(resultSet.getLong(1));
                    }
                    return null;
                }
            });
            if (lastUpdate == null || lastUpdate.isEmpty() || new File(libraryXmlUrl.getPath()).lastModified() > lastUpdate.iterator().next()) {
                return true;
            }
        }
        return false;
    }

    private URL myLibraryXmlUrl;

    public DatabaseBuilderTask(URL libraryXmlUrl) {
        myLibraryXmlUrl = libraryXmlUrl;
    }

    public void execute() {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            if (needsCreation()) {
                createDatabase();
            } else {
                storeSession.executeStatement(new PrepareForReloadStatement());
            }
            final long updateTime = System.currentTimeMillis();
            ITunesUtils.loadFromITunes(myLibraryXmlUrl, storeSession);
            storeSession.executeStatement(new UpdateHelpTablesStatement());
            createPagers(storeSession);
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE mytunes SET lastupdate = " + updateTime);
                }
            });
            storeSession.commit();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load iTunes library.", e);
            }
            try {
                storeSession.rollback();
            } catch (SQLException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not rollback transaction.", e1);
                }
            }
        }
    }

    private void createPagers(DataStoreSession storeSession) throws SQLException {
        storeSession.executeStatement(new InsertPageStatement(0, "first < 'a' OR first > 'z'", "0 - 9"));
        storeSession.executeStatement(new InsertPageStatement(1, "first >= 'a' AND first < 'd'", "A - C"));
        storeSession.executeStatement(new InsertPageStatement(2, "first >= 'd' AND first < 'g'", "D - F"));
        storeSession.executeStatement(new InsertPageStatement(3, "first >= 'g' AND first < 'j'", "G - I"));
        storeSession.executeStatement(new InsertPageStatement(4, "first >= 'j' AND first < 'm'", "J - L"));
        storeSession.executeStatement(new InsertPageStatement(5, "first >= 'm' AND first < 'p'", "M - O"));
        storeSession.executeStatement(new InsertPageStatement(6, "first >= 'p' AND first < 't'", "P - S"));
        storeSession.executeStatement(new InsertPageStatement(7, "first >= 't' AND first < 'w'", "T - V"));
        storeSession.executeStatement(new InsertPageStatement(8, "first >= 'w' AND first <= 'z'", "W - Z"));
    }

    public void createDatabase() throws SQLException {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            storeSession.executeStatement(new CreateAllTablesStatement());
            storeSession.commit();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create tables.", e);
            }
            storeSession.rollback();
            throw e;
        }
    }

    protected void cancel() {
        // intentionally left blank
    }

}