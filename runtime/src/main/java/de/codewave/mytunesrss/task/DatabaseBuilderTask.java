/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.external.ExternalLoader;
import de.codewave.mytunesrss.datastore.filesystem.FileSystemLoader;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.swing.Task;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderTask extends MyTunesRssTask {
    public static void updateHelpTables(DataStoreSession session, int updatedCount) {
        if (updatedCount % MyTunesRssDataStore.UPDATE_HELP_TABLES_FREQUENCY == 0) {
            // recreate help tables every N tracks
            try {
                session.executeStatement(new RecreateHelpTablesStatement());
                DatabaseBuilderTask.doCheckpoint(session, false);
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not recreate help tables..", e);
                }
            }
        }
    }

    public static void interruptCurrentTask() {
        final Task task = CURRENTLY_RUNNING_TASK;
        if (task != null) {
            task.interrupt();
        }
    }

    public enum State {
        UpdatingTracksFromItunes(), UpdatingTracksFromFolder(), UpdatingTrackImages(), Idle();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBuilderTask.class);
    private static Lock CURRENTLY_RUNNING = new ReentrantLock();
    private static DatabaseBuilderTask CURRENTLY_RUNNING_TASK;
    private static State myState = State.Idle;
    private static final long MAX_TX_DURATION = 2500;
    private List<File> myFileDatasources = new ArrayList<File>();
    private List<String> myExternalDatasources = new ArrayList<String>();
    private boolean myExecuted;
    private static long TX_BEGIN;

    public DatabaseBuilderTask() {
        if (MyTunesRss.CONFIG.getDatasources() != null && MyTunesRss.CONFIG.getDatasources().length > 0) {
            for (String datasource : MyTunesRss.CONFIG.getDatasources()) {
                File file = new File(datasource);
                if (!file.exists()) {
                    addToDatasources(datasource);
                } else {
                    addToDatasources(file);
                }
            }
        }
        if (StringUtils.isNotBlank(MyTunesRss.CONFIG.getUploadDir())) {
            addToDatasources(new File(StringUtils.trim(MyTunesRss.CONFIG.getUploadDir())));
        }
    }

    private void addToDatasources(String external) {
        LOGGER.debug("Adding non-file datasource \"" + external + "\" to database update sources.");
        myExternalDatasources.add(external);
    }

    private void addToDatasources(File file) {
        if (file.isDirectory()) {
            for (Iterator<File> iter = myFileDatasources.iterator(); iter.hasNext();) {
                File each = iter.next();
                if (each.isDirectory()) {
                    try {
                        if (each.equals(file) || de.codewave.utils.io.IOUtils.isContained(each, file)) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("Not adding \"" + file.getAbsolutePath() + "\" to database update sources.");
                            }
                            return; // new dir is already scanned through other dir
                        } else if (de.codewave.utils.io.IOUtils.isContained(file, each)) {
                            // existing one will be scanned by adding new one, so remove existing one
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("Removing folder \"" + each.getAbsolutePath() + "\" from database update sources.");
                            }
                            iter.remove();
                        }
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not check whether or not folder may be added, so adding it.", e);
                        }
                    }
                }
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding folder \"" + file.getAbsolutePath() + "\" to database update sources.");
            }
            myFileDatasources.add(file);
        } else if (file.isFile()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding iTunes XML file \"" + file.getAbsolutePath() + "\" to database update sources.");
            }
            myFileDatasources.add(file);
        }
    }

    public boolean needsUpdate() throws SQLException {
        if (myFileDatasources != null) {
            for (File baseDir : myFileDatasources) {
                if (baseDir.isDirectory()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Database update needed.");
                    }
                    return true;
                } else if (baseDir.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(baseDir.getName()))) {
                    DataStoreSession session = MyTunesRss.STORE.getTransaction();
                    SystemInformation systemInformation;
                    try {
                        systemInformation = session.executeQuery(new GetSystemInformationQuery());
                    } finally {
                        DatabaseBuilderTask.doCheckpoint(session, true);
                    }
                    if (MyTunesRss.CONFIG.isIgnoreTimestamps() || baseDir.lastModified() > systemInformation.getLastUpdate()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Database update needed (file datasource changed).");
                        }
                        return true;
                    }
                }
            }
        }
        if (myExternalDatasources != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Database update needed (externals have to be checked).");
            }
            return true;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Database update not necessary.");
        }
        return false;
    }

    public void execute() throws Exception {
        if (CURRENTLY_RUNNING.tryLock()) {
            setExecutionThread(Thread.currentThread());
            CURRENTLY_RUNNING_TASK = this;
            try {
                MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                event.setMessageKey("settings.databaseUpdateRunning");
                MyTunesRssEventManager.getInstance().fireEvent(event);
                internalExecute();
                myExecuted = true;
                if (!getExecutionThread().isInterrupted()) {
                    event.setMessageKey("settings.buildingTrackIndex");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
                    if (!getExecutionThread().isInterrupted()) {
                        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
                        storeSession.executeStatement(new RefreshSmartPlaylistsStatement());
                        storeSession.commit();
                    }
                }
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED);
            } catch (Exception e) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED_NOT_RUN);
                throw e;
            } finally {
                CURRENTLY_RUNNING_TASK = null;
                CURRENTLY_RUNNING.unlock();
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Database update not running since another update is still active.");
            }
        }
    }

    public boolean isExecuted() {
        return myExecuted;
    }

    public void internalExecute() throws Exception {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting database update.");
            }
            final long timeUpdateStart = System.currentTimeMillis();
            SystemInformation systemInformation = storeSession.executeQuery(new GetSystemInformationQuery());
            Map<String, Long> missingItunesFiles = runUpdate(systemInformation, storeSession);
            if (!getExecutionThread().isInterrupted()) {
                storeSession.executeStatement(new UpdateStatisticsStatement());
                storeSession.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        connection.createStatement().execute("UPDATE system_information SET lastupdate = " + timeUpdateStart);
                    }
                });
                DatabaseBuilderTask.doCheckpoint(storeSession, true);
            }
            if (!MyTunesRss.CONFIG.isIgnoreArtwork() && !getExecutionThread().isInterrupted()) {
                runImageUpdate(storeSession, systemInformation.getLastUpdate(), timeUpdateStart);
            }
            DatabaseBuilderTask.doCheckpoint(storeSession, true);
            if (!getExecutionThread().isInterrupted()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Deleting orphaned images.");
                }
                storeSession.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        MyTunesRssUtils.createStatement(connection, "deleteOrphanedImages").execute();
                    }
                });
                DatabaseBuilderTask.doCheckpoint(storeSession, true);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Creating database checkpoint.");
                    LOGGER.info("Update took " + (System.currentTimeMillis() - timeUpdateStart) + " ms.");
                }
                MyTunesRss.ADMIN_NOTIFY.notifyDatabaseUpdate((System.currentTimeMillis() - timeUpdateStart),
                        missingItunesFiles,
                        storeSession.executeQuery(new GetSystemInformationQuery()));
                storeSession.commit();
            }
        } catch (Exception e) {
            storeSession.rollback();
            throw e;
        }
    }

    private void runImageUpdate(DataStoreSession storeSession, long lastUpdateTime, final long timeUpdateStart) throws SQLException {
        myState = State.UpdatingTrackImages;
        MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
        event.setMessageKey("settings.databaseUpdateRunningImages");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        TX_BEGIN = System.currentTimeMillis();
        DataStoreSession trackQuerySession = MyTunesRss.STORE.getTransaction();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Processing track images.");
        }
        try {
            DataStoreQuery.QueryResult<Track> result = trackQuerySession.executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<Track>>() {
                public QueryResult<Track> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTracksForImageUpdate");
                    statement.setLong("timeUpdateStart", timeUpdateStart);
                    return execute(statement, new ResultBuilder<Track>() {
                        public Track create(ResultSet resultSet) throws SQLException {
                            Track track = new Track();
                            track.setId(resultSet.getString("ID"));
                            track.setSource(TrackSource.valueOf(resultSet.getString("SOURCE")));
                            track.setFile(new File(resultSet.getString("FILE")));
                            track.setLastImageUpdate(resultSet.getLong("LAST_IMAGE_UPDATE"));
                            return track;
                        }
                    });
                }
            });
            long scannedCount = 0;
            long lastEventTime = System.currentTimeMillis();
            long startTime = System.currentTimeMillis();
            for (Track track = result.nextResult(); track != null && !getExecutionThread().isInterrupted(); track = result.nextResult()) {
                scannedCount++;
                if (System.currentTimeMillis() - lastEventTime > 2500L) {
                    event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                    event.setMessageKey("settings.databaseUpdateRunningImagesWithCount");
                    event.setMessageParams(scannedCount, scannedCount / ((System.currentTimeMillis() - startTime) / 1000L));
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    lastEventTime = System.currentTimeMillis();
                }
                long timeLastImageUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : track.getLastImageUpdate();
                storeSession.executeStatement(new HandleTrackImagesStatement(track.getSource(), track.getFile(), track.getId(), timeLastImageUpdate));
                doCheckpoint(storeSession, false);
            }
        } finally {
            trackQuerySession.commit();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Finished processing track images.");
            }
        }
    }

    public static void doCheckpoint(DataStoreSession storeSession, boolean forceCommit) {
        long time = System.currentTimeMillis();
        if (TX_BEGIN == 0) {
            TX_BEGIN = time;
        }
        if (time - TX_BEGIN > MAX_TX_DURATION || forceCommit) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Committing transaction after " + (time - TX_BEGIN) + " milliseconds.");
            }
            storeSession.commit();
            TX_BEGIN = System.currentTimeMillis();
        }
    }


    /**
     * @param systemInformation
     * @param storeSession
     * @return Map with the number of missing files per iTunes XML.
     * @throws SQLException
     * @throws IOException
     */
    private Map<String, Long> runUpdate(SystemInformation systemInformation, DataStoreSession storeSession) throws SQLException, IOException {
        Map<String, Long> missingItunesFiles = new HashMap<String, Long>();
        long timeLastUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : systemInformation.getLastUpdate();
        Collection<String> itunesPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunes.name()));
        itunesPlaylistIds.addAll(storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunesFolder.name())));
        Collection<String> m3uPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.M3uFile.name()));
        final Set<String> trackIds = storeSession.executeQuery(new DataStoreQuery<Set<String>>() {
            public Set<String> execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getTrackIds");
                ResultSet rs = statement.executeQuery();
                Set<String> ids = new HashSet<String>();
                while (rs.next()) {
                    ids.add(rs.getString("id"));
                }
                return ids;
            }
        });
        if (myFileDatasources != null && !getExecutionThread().isInterrupted()) {
            for (File datasource : myFileDatasources) {
                doCheckpoint(storeSession, false);
                if (datasource.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(datasource.getName())) && !getExecutionThread().isInterrupted()) {
                    myState = State.UpdatingTracksFromItunes;
                    MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                    event.setMessageKey("settings.databaseUpdateRunningItunes");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    missingItunesFiles.put(datasource.getCanonicalPath(), ItunesLoader.loadFromITunes(getExecutionThread(), datasource.toURL(),
                            storeSession,
                            timeLastUpdate,
                            trackIds,
                            itunesPlaylistIds));
                } else if (datasource.isDirectory() && !getExecutionThread().isInterrupted()) {
                    try {
                        myState = State.UpdatingTracksFromFolder;
                        MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                        event.setMessageKey("settings.databaseUpdateRunningFolder");
                        MyTunesRssEventManager.getInstance().fireEvent(event);
                        FileSystemLoader.loadFromFileSystem(getExecutionThread(), datasource, storeSession, timeLastUpdate, trackIds, m3uPlaylistIds);
                    } catch (ShutdownRequestedException e) {
                        // intentionally left blank
                    }
                }
            }
            DatabaseBuilderTask.doCheckpoint(storeSession, true);
        }
        if (myExternalDatasources != null && !getExecutionThread().isInterrupted()) {
            for (String external : myExternalDatasources) {
                doCheckpoint(storeSession, false);
                MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                event.setMessageKey("settings.databaseUpdateRunningExternal");
                MyTunesRssEventManager.getInstance().fireEvent(event);
                ExternalLoader.process(StringUtils.trim(external), storeSession, timeLastUpdate, trackIds);
            }
        }
        if (!getExecutionThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + trackIds.size() + " tracks from database.");
            }
            storeSession.executeStatement(new RemoveTrackStatement(trackIds));
            DatabaseBuilderTask.doCheckpoint(storeSession, true);
        }
        if (!getExecutionThread().isInterrupted()) {
            // ensure the help tables are created with all the data
            storeSession.executeStatement(new RecreateHelpTablesStatement());
            DatabaseBuilderTask.doCheckpoint(storeSession, true);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Removing " + (itunesPlaylistIds.size() + m3uPlaylistIds.size()) + " playlists from database.");
            }
        }
        if (!itunesPlaylistIds.isEmpty() && !getExecutionThread().isInterrupted()) {
            removeObsoletePlaylists(storeSession, itunesPlaylistIds);
        }
        if (!m3uPlaylistIds.isEmpty() && !getExecutionThread().isInterrupted()) {
            removeObsoletePlaylists(storeSession, m3uPlaylistIds);
        }
        DatabaseBuilderTask.doCheckpoint(storeSession, true);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Obsolete tracks and playlists removed from database.");
        }
        return missingItunesFiles;
    }

    private void removeObsoletePlaylists(DataStoreSession storeSession, Collection<String> databaseIds) throws SQLException {
        DeletePlaylistStatement statement = new DeletePlaylistStatement();
        for (String id : databaseIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            DatabaseBuilderTask.doCheckpoint(storeSession, false);
        }
    }

    public static boolean isRunning() {
        boolean locked = CURRENTLY_RUNNING.tryLock();
        if (locked) {
            CURRENTLY_RUNNING.unlock();
        }
        return !locked;
    }

    public static State getState() {
        return myState;
    }
}