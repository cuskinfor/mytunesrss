/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.filesystem.*;
import de.codewave.mytunesrss.datastore.itunes.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderTask extends MyTunesRssTask {
    private static final Log LOG = LogFactory.getLog(DatabaseBuilderTask.class);
    private static boolean CURRENTLY_RUNNING;

    private URL myLibraryXmlUrl;
    private List<File> myBaseDirs = new ArrayList<File>();
    private boolean myExecuted;

    public DatabaseBuilderTask() {
        try {
            myLibraryXmlUrl = StringUtils.isNotEmpty(MyTunesRss.CONFIG.getLibraryXml()) ? new File(MyTunesRss.CONFIG.getLibraryXml().trim()).toURL() :
                    null;
        } catch (MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create URL from iTunes XML file.", e);
            }
        }
        if (MyTunesRss.CONFIG.getWatchFolders() != null && MyTunesRss.CONFIG.getWatchFolders().length > 0) {
            for (String baseDir : MyTunesRss.CONFIG.getWatchFolders()) {
                myBaseDirs.add(new File(baseDir));
            }
        }
        if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getUploadDir())) {
            myBaseDirs.add(new File(MyTunesRss.CONFIG.getUploadDir().trim()));
        }
    }

    public boolean needsUpdate() throws SQLException {
        if (myBaseDirs != null) {
            for (File baseDir : myBaseDirs) {
                if (baseDir.isDirectory() && baseDir.exists()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Database update needed.");
                    }
                    return true;
                }
            }
        }
        if (myLibraryXmlUrl != null) {
            SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
            if (MyTunesRss.CONFIG.isIgnoreTimestamps() || new File(myLibraryXmlUrl.getPath()).lastModified() > systemInformation.getLastUpdate()) {
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

    public void execute() throws Exception {
        boolean execute = false;
        if (!CURRENTLY_RUNNING) {
            synchronized (DatabaseBuilderTask.class) {
                if (!CURRENTLY_RUNNING) {
                    CURRENTLY_RUNNING = true;
                    execute = true;
                }
            }
        }
        if (execute) {
            try {
                internalExecute();
                myExecuted = true;
            } finally {
                CURRENTLY_RUNNING = false;
            }
        }
    }

    public boolean isExecuted() {
        return myExecuted;
    }

    public void internalExecute() throws Exception {
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
            long timeLastUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : systemInformation.getLastUpdate();
            final String libraryId = ItunesLoader.loadFromITunes(myLibraryXmlUrl,
                                                                 storeSession,
                                                                 systemInformation.getItunesLibraryId(),
                                                                 timeLastUpdate);
            FileSystemLoader.loadFromFileSystem(myBaseDirs, storeSession, timeLastUpdate);
            storeSession.commitAndContinue();
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
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE system_information SET lastupdate = " + timeUpdateStart);
                    connection.createStatement().execute("UPDATE system_information SET itunes_library_id = '" + libraryId + "'");
                }
            });
            if (LOG.isDebugEnabled()) {
                LOG.debug("Committing transaction.");
            }
            storeSession.commit();
            long timeAfterCommit = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Time for commit: " + (timeAfterCommit - timeAfterHelpTables));
                LOG.debug("Creating database checkpoint.");
            }
        } catch (Exception e) {
            storeSession.rollback();
            throw e;
        }
    }

    public boolean isRunning() {
        return CURRENTLY_RUNNING;
    }
}