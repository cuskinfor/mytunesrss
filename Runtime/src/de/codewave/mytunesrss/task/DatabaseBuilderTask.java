/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderTask extends PleaseWait.NoCancelTask {
    private static final Log LOG = LogFactory.getLog(DatabaseBuilderTask.class);

    public static boolean needsUpdate(URL libraryXmlUrl) throws SQLException {
        if (libraryXmlUrl != null) {
            SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
            if (new File(libraryXmlUrl.getPath()).lastModified() > systemInformation.getLastUpdate()) {
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
    private Options myOptionsForm;

    public DatabaseBuilderTask(URL libraryXmlUrl, Options optionsForm) {
        myLibraryXmlUrl = libraryXmlUrl;
        myOptionsForm = optionsForm;
    }

    public void execute() throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Database builder task started.");
        }
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Preparing database tables for update.");
            }
            storeSession.executeStatement(new PrepareForUpdateStatement());
            final long timeUpdateStart = System.currentTimeMillis();
            SystemInformation systemInformation = storeSession.executeQuery(new GetSystemInformationQuery());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading tracks from iTunes library.");
            }
            final String libraryId = ITunesUtils.loadFromITunes(myLibraryXmlUrl, storeSession, systemInformation.getItunesLibraryId(), systemInformation.getLastUpdate());
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
                    connection.createStatement().execute("UPDATE system_information SET lastupdate = " + timeUpdateStart);
                    connection.createStatement().execute("UPDATE system_information SET itunes_library_id = '" + libraryId + "'");
                }
            });
            if (LOG.isDebugEnabled()) {
                LOG.debug("Committing transaction.");
            }
            storeSession.commitAndContinue();
            long timeAfterCommit = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Time for commit: " + (timeAfterCommit - timeAfterHelpTables));
                LOG.debug("Creating database checkpoint.");
            }
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("CHECKPOINT");
                }
            });
            long timeAfterCheckpoint = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("time for creating checkpoint: " + (timeAfterCheckpoint - timeAfterCommit));
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    myOptionsForm.refreshLastUpdate();
                }
            });
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