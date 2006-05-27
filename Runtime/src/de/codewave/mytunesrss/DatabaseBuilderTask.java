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

    public static enum BuildType {
        Recreate("Recreating"),Refresh("Refreshing"),Update("Updating");

        private String myVerb;

        BuildType(String verb) {
            myVerb = verb;
        }

        public String getVerb() {
            return myVerb;
        }
    }

    public static boolean needsUpdate(URL libraryXmlUrl) throws SQLException {
        if (libraryXmlUrl != null) {
            Collection<Long> lastUpdate = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Long>() {
                public Collection<Long> execute(Connection connection) throws SQLException {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT lastupdate FROM mytunesrss");
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
    private BuildType myBuildType;

    public DatabaseBuilderTask(URL libraryXmlUrl, BuildType buildType) {
        myLibraryXmlUrl = libraryXmlUrl;
        myBuildType = buildType;
    }

    public void execute() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Building database.");
        }
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            if (myBuildType == BuildType.Recreate) {
                deleteAllContent();
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Preparing database tables for refresh/update.");
            }
            storeSession.executeStatement(new PrepareForUpdateStatement());
            final long timeUpdateStart = System.currentTimeMillis();
            Long timeLastUpdate = Long.MIN_VALUE;
            if (myBuildType == BuildType.Update) {
                timeLastUpdate = storeSession.getFirstQueryResult(new DataStoreQuery<Long>() {
                    public Collection<Long> execute(Connection connection) throws SQLException {
                        ResultSet resultSet = connection.createStatement().executeQuery("SELECT lastupdate AS lastupdate FROM mytunesrss");
                        if (resultSet.next()) {
                            return Collections.singletonList(resultSet.getLong("LASTUPDATE"));
                        }
                        return Collections.singletonList(Long.MIN_VALUE);
                    }
                });
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Loading tracks from iTunes library.");
            }
            ITunesUtils.loadFromITunes(myLibraryXmlUrl, storeSession, timeLastUpdate.longValue());
            long timeAfterTracks = System.currentTimeMillis();
            if (LOG.isInfoEnabled()) {
                LOG.info("Time for loading tracks: " + (timeAfterTracks - timeUpdateStart));
                LOG.info("Building help tables.");
            }
            storeSession.executeStatement(new UpdateHelpTablesStatement(storeSession.executeQuery(new FindAlbumArtistMappingQuery())));
            long timeAfterHelpTables = System.currentTimeMillis();
            if (LOG.isInfoEnabled()) {
                LOG.info("Time for building help tables: " + (timeAfterHelpTables - timeAfterTracks));
                LOG.info("Building pager and updating system information.");
            }
            createPagers(storeSession);
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE mytunesrss SET lastupdate = " + timeUpdateStart);
                }
            });
            if (LOG.isInfoEnabled()) {
                LOG.info("Committing transaction.");
            }
            storeSession.commit();
            long timeAfterCommit = System.currentTimeMillis();
            if (LOG.isInfoEnabled()) {
                LOG.info("time for commit: " + (timeAfterCommit - timeAfterHelpTables));
            }
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
        storeSession.executeStatement(new InsertPageStatement(0, "first_char < 'a' OR first_char > 'z'", "0 - 9"));
        storeSession.executeStatement(new InsertPageStatement(1, "first_char >= 'a' AND first_char < 'd'", "A - C"));
        storeSession.executeStatement(new InsertPageStatement(2, "first_char >= 'd' AND first_char < 'g'", "D - F"));
        storeSession.executeStatement(new InsertPageStatement(3, "first_char >= 'g' AND first_char < 'j'", "G - I"));
        storeSession.executeStatement(new InsertPageStatement(4, "first_char >= 'j' AND first_char < 'm'", "J - L"));
        storeSession.executeStatement(new InsertPageStatement(5, "first_char >= 'm' AND first_char < 'p'", "M - O"));
        storeSession.executeStatement(new InsertPageStatement(6, "first_char >= 'p' AND first_char < 't'", "P - S"));
        storeSession.executeStatement(new InsertPageStatement(7, "first_char >= 't' AND first_char < 'w'", "T - V"));
        storeSession.executeStatement(new InsertPageStatement(8, "first_char >= 'w' AND first_char <= 'z'", "W - Z"));
    }

    public void deleteAllContent() throws SQLException {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        storeSession.executeStatement(new DeleteAllContentStatement());
        storeSession.commit();
    }

    protected void cancel() {
        // intentionally left blank
    }

}