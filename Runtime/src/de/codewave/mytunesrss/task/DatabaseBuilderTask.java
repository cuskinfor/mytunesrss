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
import org.apache.commons.io.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderTask extends MyTunesRssTask {
    private static final Log LOG = LogFactory.getLog(DatabaseBuilderTask.class);
    private static boolean CURRENTLY_RUNNING;

    private List<File> myBaseDirs = new ArrayList<File>();
    private boolean myExecuted;

    public DatabaseBuilderTask() {
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
                } else if (baseDir.isFile() && baseDir.exists() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(baseDir.getName()))) {
                    SystemInformation systemInformation = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery());
                    if (MyTunesRss.CONFIG.isIgnoreTimestamps() || baseDir.lastModified() > systemInformation.getLastUpdate()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Database update needed.");
                        }
                        return true;
                    }
                }
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
            Set<String> databaseIds = (Set<String>)storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.ITunes.name()));
            databaseIds.addAll((Set<String>)storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.FileSystem.name())));
            if (MyTunesRss.CONFIG.getWatchFolders() != null) {
                for (String filePath : MyTunesRss.CONFIG.getWatchFolders()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Parsing \"" + filePath + "\".");
                    }
                    File file = new File(filePath);
                    if (file.isFile() && file.exists() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                        ItunesLoader.loadFromITunes(file.toURL(), storeSession, timeLastUpdate, databaseIds);
                    } else if (file.isDirectory() && file.exists()) {
                        FileSystemLoader.loadFromFileSystem(file, storeSession, timeLastUpdate, databaseIds);
                    }
                }}
            if (!databaseIds.isEmpty()) {
                removeObsoleteTracks(storeSession, databaseIds);
            }
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

    private static void removeObsoleteTracks(DataStoreSession storeSession, Set<String> databaseIds) throws SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Removing " + databaseIds.size() + " obsolete tracks.");
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

    public boolean isRunning() {
        return CURRENTLY_RUNNING;
    }
}