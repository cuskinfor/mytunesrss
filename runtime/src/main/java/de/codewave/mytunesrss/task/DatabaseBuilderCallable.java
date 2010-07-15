/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import de.codewave.mytunesrss.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.external.ExternalLoader;
import de.codewave.mytunesrss.datastore.filesystem.FileSystemLoader;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.datastore.statement.DeletePlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.HandleTrackImagesStatement;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.RecreateHelpTablesStatement;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.mytunesrss.datastore.statement.RemoveTrackStatement;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.mytunesrss.datastore.statement.UpdateStatisticsStatement;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderCallable implements Callable<Boolean> {

    public static void updateHelpTables(DataStoreSession session, int updatedCount) {
        if (updatedCount % MyTunesRssDataStore.UPDATE_HELP_TABLES_FREQUENCY == 0) {
            // recreate help tables every N tracks
            try {
                session.executeStatement(new RecreateHelpTablesStatement());
                DatabaseBuilderCallable.doCheckpoint(session, false);
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not recreate help tables..", e);
                }
            }
        }
    }

    public enum State {
        UpdatingTracksFromItunes(), UpdatingTracksFromFolder(), UpdatingTrackImages(), Idle();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBuilderCallable.class);

    private static State myState = State.Idle;

    private static final long MAX_TX_DURATION = 2500;

    private List<File> myFileDatasources = new ArrayList<File>();

    private List<String> myExternalDatasources = new ArrayList<String>();

    private static long TX_BEGIN;

    public DatabaseBuilderCallable() {
        if (MyTunesRss.CONFIG.getDatasources() != null && MyTunesRss.CONFIG.getDatasources().size() > 0) {
            for (DatasourceConfig datasource : MyTunesRss.CONFIG.getDatasources()) {
                File file = new File(datasource.getDefinition());
                if (!file.exists()) {
                    addToDatasources(datasource.getDefinition());
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
                        if (de.codewave.utils.io.IOUtils.isContainedOrSame(each, file)) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER
                                        .info("Not adding \"" + file.getAbsolutePath()
                                                + "\" to database update sources.");
                            }
                            return; // new dir is already scanned through other dir
                        } else if (de.codewave.utils.io.IOUtils.isContained(file, each)) {
                            // existing one will be scanned by adding new one, so remove existing one
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("Removing folder \"" + each.getAbsolutePath()
                                        + "\" from database update sources.");
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
                        DatabaseBuilderCallable.doCheckpoint(session, true);
                    }
                    if (MyTunesRss.CONFIG.isIgnoreTimestamps()
                            || baseDir.lastModified() > systemInformation.getLastUpdate()) {
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

    public Boolean call() throws Exception {
        Boolean result = Boolean.FALSE;
        try {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
            event.setMessageKey("settings.databaseUpdateRunning");
            MyTunesRssEventManager.getInstance().fireEvent(event);
            internalExecute();
            result = Boolean.TRUE;
            if (!Thread.currentThread().isInterrupted()) {
                DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
                storeSession.executeStatement(new RefreshSmartPlaylistsStatement());
                storeSession.commit();
            }
            MyTunesRssEventManager.getInstance().fireEvent(
                    MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        } catch (Exception e) {
            MyTunesRssEventManager.getInstance().fireEvent(
                    MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED_NOT_RUN));
            throw e;
        }
        return result;
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
            if (!Thread.currentThread().isInterrupted()) {
                storeSession.executeStatement(new UpdateStatisticsStatement());
                storeSession.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        connection.createStatement().execute(
                                "UPDATE system_information SET lastupdate = " + timeUpdateStart);
                    }
                });
                DatabaseBuilderCallable.doCheckpoint(storeSession, true);
            }
            if (!MyTunesRss.CONFIG.isIgnoreArtwork() && !Thread.currentThread().isInterrupted()) {
                runImageUpdate(storeSession, systemInformation.getLastUpdate(), timeUpdateStart);
            }
            updateHelpTables(storeSession, 0); // update image references for albums
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
            if (!Thread.currentThread().isInterrupted()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Deleting orphaned images.");
                }
                storeSession.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        MyTunesRssUtils.createStatement(connection, "deleteOrphanedImages").execute();
                    }
                });
                DatabaseBuilderCallable.doCheckpoint(storeSession, true);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Creating database checkpoint.");
                    LOGGER.info("Update took " + (System.currentTimeMillis() - timeUpdateStart) + " ms.");
                }
                MyTunesRss.ADMIN_NOTIFY.notifyDatabaseUpdate((System.currentTimeMillis() - timeUpdateStart),
                        missingItunesFiles, storeSession.executeQuery(new GetSystemInformationQuery()));
                storeSession.commit();
            }
        } catch (Exception e) {
            storeSession.rollback();
            throw e;
        }
    }

    private void runImageUpdate(DataStoreSession storeSession, long lastUpdateTime, final long timeUpdateStart)
            throws SQLException {
        myState = State.UpdatingTrackImages;
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
        event.setMessageKey("settings.databaseUpdateRunningImages");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        TX_BEGIN = System.currentTimeMillis();
        DataStoreSession trackQuerySession = MyTunesRss.STORE.getTransaction();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Processing track images.");
        }
        try {
            DataStoreQuery.QueryResult<Track> result = trackQuerySession
                    .executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<Track>>() {
                        public QueryResult<Track> execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection,
                                    "findAllTracksForImageUpdate");
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
            for (Track track = result.nextResult(); track != null && !Thread.currentThread().isInterrupted(); track = result
                    .nextResult()) {
                scannedCount++;
                if (System.currentTimeMillis() - lastEventTime > 2500L) {
                    event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
                    event.setMessageKey("settings.databaseUpdateRunningImagesWithCount");
                    event.setMessageParams(scannedCount, scannedCount
                            / ((System.currentTimeMillis() - startTime) / 1000L));
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    lastEventTime = System.currentTimeMillis();
                }
                long timeLastImageUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : track
                        .getLastImageUpdate();
                storeSession.executeStatement(new HandleTrackImagesStatement(track.getSource(), track.getFile(), track
                        .getId(), timeLastImageUpdate));
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
    private Map<String, Long> runUpdate(SystemInformation systemInformation, DataStoreSession storeSession)
            throws SQLException, IOException {
        Map<String, Long> missingItunesFiles = new HashMap<String, Long>();
        long timeLastUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : systemInformation
                .getLastUpdate();
        Collection<String> itunesPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunes
                .name()));
        itunesPlaylistIds.addAll(storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunesFolder.name())));
        Collection<String> m3uPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.M3uFile
                .name()));
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
        if (myFileDatasources != null && !Thread.currentThread().isInterrupted()) {
            for (File datasource : myFileDatasources) {
                doCheckpoint(storeSession, false);
                if (datasource.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(datasource.getName()))
                        && !Thread.currentThread().isInterrupted()) {
                    myState = State.UpdatingTracksFromItunes;
                    MyTunesRssEvent event = MyTunesRssEvent
                            .create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
                    event.setMessageKey("settings.databaseUpdateRunningItunes");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    missingItunesFiles.put(datasource.getCanonicalPath(), ItunesLoader.loadFromITunes(Thread
                            .currentThread(), datasource.toURL(), storeSession, timeLastUpdate, trackIds,
                            itunesPlaylistIds));
                } else if (datasource.isDirectory() && !Thread.currentThread().isInterrupted()) {
                    try {
                        myState = State.UpdatingTracksFromFolder;
                        MyTunesRssEvent event = MyTunesRssEvent
                                .create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
                        event.setMessageKey("settings.databaseUpdateRunningFolder");
                        MyTunesRssEventManager.getInstance().fireEvent(event);
                        FileSystemLoader.loadFromFileSystem(Thread.currentThread(), datasource, storeSession,
                                timeLastUpdate, trackIds, m3uPlaylistIds);
                    } catch (ShutdownRequestedException e) {
                        // intentionally left blank
                    }
                }
            }
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        }
        if (myExternalDatasources != null && !Thread.currentThread().isInterrupted()) {
            for (String external : myExternalDatasources) {
                doCheckpoint(storeSession, false);
                MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
                event.setMessageKey("settings.databaseUpdateRunningExternal");
                MyTunesRssEventManager.getInstance().fireEvent(event);
                ExternalLoader.process(StringUtils.trim(external), storeSession, timeLastUpdate, trackIds);
            }
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + trackIds.size() + " tracks from database.");
            }
            storeSession.executeStatement(new RemoveTrackStatement(trackIds));
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        }
        if (!Thread.currentThread().isInterrupted()) {
            // ensure the help tables are created with all the data
            updateHelpTables(storeSession, 0);
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Removing " + (itunesPlaylistIds.size() + m3uPlaylistIds.size())
                        + " playlists from database.");
            }
        }
        if (!itunesPlaylistIds.isEmpty() && !Thread.currentThread().isInterrupted()) {
            removeObsoletePlaylists(storeSession, itunesPlaylistIds);
        }
        if (!m3uPlaylistIds.isEmpty() && !Thread.currentThread().isInterrupted()) {
            removeObsoletePlaylists(storeSession, m3uPlaylistIds);
        }
        DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Obsolete tracks and playlists removed from database.");
        }
        if (!Thread.currentThread().isInterrupted()) {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED);
            event.setMessageKey("settings.buildingTrackIndex");
            MyTunesRssEventManager.getInstance().fireEvent(event);
            MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        }
        return missingItunesFiles;
    }

    private void removeObsoletePlaylists(DataStoreSession storeSession, Collection<String> databaseIds)
            throws SQLException {
        DeletePlaylistStatement statement = new DeletePlaylistStatement();
        for (String id : databaseIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            DatabaseBuilderCallable.doCheckpoint(storeSession, false);
        }
    }

    public static State getState() {
        return myState;
    }
}
