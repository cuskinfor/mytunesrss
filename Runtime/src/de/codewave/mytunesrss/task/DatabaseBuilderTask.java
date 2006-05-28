/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 *de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderTask extends PleaseWait.NoCancelTask {
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Database update needed.");
                }
                return true;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Database update not necessary.");
        }
        return false;
    }

    private URL myLibraryXmlUrl;
    private BuildType myBuildType;

    public DatabaseBuilderTask(URL libraryXmlUrl, BuildType buildType) {
        myLibraryXmlUrl = libraryXmlUrl;
        myBuildType = buildType;
    }

    public void execute() throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Database builder task started.");
        }
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            if (myBuildType == BuildType.Recreate) {
                deleteAllContent();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Preparing database tables for refresh/update.");
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading tracks from iTunes library.");
            }
            ITunesUtils.loadFromITunes(myLibraryXmlUrl, storeSession, timeLastUpdate.longValue());
            long timeAfterTracks = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Time for loading tracks: " + (timeAfterTracks - timeUpdateStart));
                LOG.debug("Building help tables.");
            }
            storeSession.executeStatement(new UpdateHelpTablesStatement(storeSession.executeQuery(new FindAlbumArtistMappingQuery())));
            long timeAfterHelpTables = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Time for building help tables: " + (timeAfterHelpTables - timeAfterTracks));
                LOG.debug("Building pager and updating system information.");
            }
            createAlbumPager(storeSession);
            createArtistPager(storeSession);
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE mytunesrss SET lastupdate = " + timeUpdateStart);
                }
            });
            if (LOG.isDebugEnabled()) {
                LOG.debug("Committing transaction.");
            }
            storeSession.commit();
            long timeAfterCommit = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("time for commit: " + (timeAfterCommit - timeAfterHelpTables));
            }
        } catch (SQLException e) {
            storeSession.rollback();
            throw e;
        }
    }

    private void createAlbumPager(DataStoreSession storeSession) throws SQLException {
        insertAlbumPage(storeSession, 0, "first_char < 'a' OR first_char > 'z'", "0 - 9");
        insertAlbumPage(storeSession, 1, "first_char >= 'a' AND first_char < 'd'", "A - C");
        insertAlbumPage(storeSession, 2, "first_char >= 'd' AND first_char < 'g'", "D - F");
        insertAlbumPage(storeSession, 3, "first_char >= 'g' AND first_char < 'j'", "G - I");
        insertAlbumPage(storeSession, 4, "first_char >= 'j' AND first_char < 'm'", "J - L");
        insertAlbumPage(storeSession, 5, "first_char >= 'm' AND first_char < 'p'", "M - O");
        insertAlbumPage(storeSession, 6, "first_char >= 'p' AND first_char < 't'", "P - S");
        insertAlbumPage(storeSession, 7, "first_char >= 't' AND first_char < 'w'", "T - V");
        insertAlbumPage(storeSession, 8, "first_char >= 'w' AND first_char <= 'z'", "W - Z");
    }

    private void insertAlbumPage(DataStoreSession storeSession, final int index, String condition, String value) throws SQLException {
        storeSession.executeStatement(new InsertPageStatement(InsertPageStatement.PagerType.Album, index, condition, value, 0));
        final int count = storeSession.executeQuery(new FindAlbumQuery(index)).size();
        storeSession.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement update = connection.prepareStatement("UPDATE pager SET content_count = ? WHERE index = ? AND type = ?");
                update.setInt(1, count);
                update.setInt(2, index);
                update.setString(3, InsertPageStatement.PagerType.Album.name());
                update.execute();
            }
        });
    }

    private void createArtistPager(DataStoreSession storeSession) throws SQLException {
        insertArtistPage(storeSession, 0, "first_char < 'a' OR first_char > 'z'", "0 - 9");
        insertArtistPage(storeSession, 1, "first_char >= 'a' AND first_char < 'd'", "A - C");
        insertArtistPage(storeSession, 2, "first_char >= 'd' AND first_char < 'g'", "D - F");
        insertArtistPage(storeSession, 3, "first_char >= 'g' AND first_char < 'j'", "G - I");
        insertArtistPage(storeSession, 4, "first_char >= 'j' AND first_char < 'm'", "J - L");
        insertArtistPage(storeSession, 5, "first_char >= 'm' AND first_char < 'p'", "M - O");
        insertArtistPage(storeSession, 6, "first_char >= 'p' AND first_char < 't'", "P - S");
        insertArtistPage(storeSession, 7, "first_char >= 't' AND first_char < 'w'", "T - V");
        insertArtistPage(storeSession, 8, "first_char >= 'w' AND first_char <= 'z'", "W - Z");
    }

    private void insertArtistPage(DataStoreSession storeSession, final int index, String condition, String value) throws SQLException {
        storeSession.executeStatement(new InsertPageStatement(InsertPageStatement.PagerType.Artist, index, condition, value, 0));
        final int count = storeSession.executeQuery(new FindArtistQuery(index)).size();
        storeSession.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement update = connection.prepareStatement("UPDATE pager SET content_count = ? WHERE index = ? AND type = ?");
                update.setInt(1, count);
                update.setInt(2, index);
                update.setString(3, InsertPageStatement.PagerType.Artist.name());
                update.execute();
            }
        });
    }

    public void deleteAllContent() throws SQLException {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        storeSession.executeStatement(new DeleteAllContentStatement());
        storeSession.commit();
    }
}