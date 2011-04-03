/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.filesystem.FileSystemLoader;
import de.codewave.mytunesrss.datastore.iphoto.IphotoLoader;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
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
import java.util.concurrent.Callable;

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
        UpdatingTracksFromItunes(), UpdatingTracksFromFolder(), UpdatingTrackImages(), Idle(), UpdatingTracksFromIphoto();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBuilderCallable.class);

    private static State myState = State.Idle;

    private static final long MAX_TX_DURATION = 2500;

    private List<DatasourceConfig> myFileDatasources = new ArrayList<DatasourceConfig>();

    private static long TX_BEGIN;

    private boolean myIgnoreTimestamps;

    public DatabaseBuilderCallable(boolean ignoreTimestamps) {
        myIgnoreTimestamps = ignoreTimestamps;
        if (MyTunesRss.CONFIG.getDatasources() != null && MyTunesRss.CONFIG.getDatasources().size() > 0) {
            for (DatasourceConfig datasource : MyTunesRss.CONFIG.getDatasources()) {
                addToDatasources(datasource);
            }
        }
        if (StringUtils.isNotBlank(MyTunesRss.CONFIG.getUploadDir())) {
            addToDatasources(new WatchfolderDatasourceConfig(MyTunesRss.CONFIG.getUploadDir()));
        }
    }

    private void addToDatasources(DatasourceConfig datasource) {
        File file = new File(datasource.getDefinition());
        if (datasource.getType() == DatasourceType.Watchfolder && file.exists()) {
            for (Iterator<DatasourceConfig> iter = myFileDatasources.iterator(); iter.hasNext();) {
                DatasourceConfig eachDatasource = iter.next();
                File eachFile = new File(eachDatasource.getDefinition());
                if (eachDatasource.getType() == DatasourceType.Watchfolder && eachFile.exists()) {
                    try {
                        if (de.codewave.utils.io.IOUtils.isContainedOrSame(eachFile, file)) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER
                                        .info("Not adding \"" + file.getAbsolutePath()
                                                + "\" to database update sources.");
                            }
                            return; // new dir is already scanned through other dir
                        } else if (de.codewave.utils.io.IOUtils.isContained(file, eachFile)) {
                            // existing one will be scanned by adding new one, so remove existing one
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("Removing folder \"" + eachFile.getAbsolutePath()
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
            myFileDatasources.add(datasource);
        } else if (datasource.getType() == DatasourceType.Itunes && file.exists()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding iTunes XML file \"" + file.getAbsolutePath() + "\" to database update sources.");
            }
            myFileDatasources.add(datasource);
        } else if (datasource.getType() == DatasourceType.Iphoto && new File(file, IphotoDatasourceConfig.XML_FILE_NAME).isFile()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding iPhoto XML file \"" + new File(file, IphotoDatasourceConfig.XML_FILE_NAME).getAbsolutePath() + "\" to database update sources.");
            }
            myFileDatasources.add(datasource);
        }
    }

    public boolean needsUpdate() throws SQLException {
        if (myFileDatasources != null) {
            for (DatasourceConfig datasource : myFileDatasources) {
                if (datasource.getType() == DatasourceType.Watchfolder) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Database update needed.");
                    }
                    return true;
                } else if (datasource.getType() == DatasourceType.Itunes || datasource.getType() == DatasourceType.Iphoto) {
                    SystemInformation systemInformation;
                    DataStoreSession session = MyTunesRss.STORE.getTransaction();
                    try {
                        systemInformation = session.executeQuery(new GetSystemInformationQuery());
                    } finally {
                        DatabaseBuilderCallable.doCheckpoint(session, true);
                    }
                    File file = datasource.getType() == DatasourceType.Itunes ? new File(datasource.getDefinition()) : new File(datasource.getDefinition(), IphotoDatasourceConfig.XML_FILE_NAME);
                    if (myIgnoreTimestamps || file.lastModified() > systemInformation.getLastUpdate()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Database update needed (file datasource changed).");
                        }
                        return true;
                    }
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Database update not necessary.");
        }
        return false;
    }

    public Boolean call() throws Exception {
        Boolean result = Boolean.FALSE;
        try {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunning");
            MyTunesRssEventManager.getInstance().fireEvent(event);
            MyTunesRss.LAST_DATABASE_EVENT = event;
            internalExecute();
            result = Boolean.TRUE;
            if (!Thread.currentThread().isInterrupted()) {
                DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
                try {
                    storeSession.executeStatement(new RefreshSmartPlaylistsStatement());
                    storeSession.commit();
                } finally {
                    storeSession.rollback();
                }
            }
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        } catch (Exception e) {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception during import.", e);
            }

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
                runImageUpdate(storeSession, timeUpdateStart);
            }
            updateHelpTables(storeSession, 0); // update image references for albums
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
            if (!Thread.currentThread().isInterrupted()) {
                deleteOrphanedImages(storeSession);
                DatabaseBuilderCallable.doCheckpoint(storeSession, true);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Update took " + (System.currentTimeMillis() - timeUpdateStart) + " ms.");
                }
                MyTunesRss.ADMIN_NOTIFY.notifyDatabaseUpdate((System.currentTimeMillis() - timeUpdateStart),
                        missingItunesFiles, storeSession.executeQuery(new GetSystemInformationQuery()));
                storeSession.commit();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            storeSession.rollback();
        }
    }

    protected void deleteOrphanedImages(DataStoreSession storeSession) throws SQLException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Deleting orphaned images.");
        }
        storeSession.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                MyTunesRssUtils.createStatement(connection, "deleteOrphanedImages").execute();
            }
        });
    }

    protected void runImageUpdate(DataStoreSession storeSession, final long timeUpdateStart)
            throws SQLException {
        myState = State.UpdatingTrackImages;
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningImages");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        MyTunesRss.LAST_DATABASE_EVENT = event;
        TX_BEGIN = System.currentTimeMillis();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Processing track images.");
        }
        DataStoreSession trackQuerySession = MyTunesRss.STORE.getTransaction();
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
            for (Track track = result.nextResult(); track != null && !Thread.currentThread().isInterrupted(); track = result
                    .nextResult()) {
                long timeLastImageUpdate = myIgnoreTimestamps ? Long.MIN_VALUE : track.getLastImageUpdate();
                storeSession.executeStatement(new HandleTrackImagesStatement(track.getSource(), track.getFile(), track
                        .getId(), timeLastImageUpdate, track.getMediaType() == MediaType.Image));
                doCheckpoint(storeSession, false);
            }
        } finally {
            trackQuerySession.rollback();
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
        long timeLastUpdate = myIgnoreTimestamps ? Long.MIN_VALUE : systemInformation
                .getLastUpdate();
        Collection<String> itunesPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunes
                .name()));
        Collection<String> iphotoAlbumIds = storeSession.executeQuery(new FindPhotoAlbumIdsQuery());
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
        final Set<String> photoIds = storeSession.executeQuery(new DataStoreQuery<Set<String>>() {
            public Set<String> execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotoIds");
                ResultSet rs = statement.executeQuery();
                Set<String> ids = new HashSet<String>();
                while (rs.next()) {
                    ids.add(rs.getString("id"));
                }
                return ids;
            }
        });
        if (myFileDatasources != null && !Thread.currentThread().isInterrupted()) {
            for (DatasourceConfig datasource : myFileDatasources) {
                doCheckpoint(storeSession, false);
                if (datasource.getType() == DatasourceType.Itunes && !Thread.currentThread().isInterrupted()) {
                    myState = State.UpdatingTracksFromItunes;
                    MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningItunes");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    MyTunesRss.LAST_DATABASE_EVENT = event;
                    missingItunesFiles.put(new File(datasource.getDefinition()).getCanonicalPath(), ItunesLoader.loadFromITunes(Thread
                            .currentThread(), (ItunesDatasourceConfig) datasource, storeSession, timeLastUpdate, photoIds,
                            itunesPlaylistIds));
                } else if (datasource.getType() == DatasourceType.Iphoto && !Thread.currentThread().isInterrupted()) {
                    myState = State.UpdatingTracksFromIphoto;
                    MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningIphoto");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    MyTunesRss.LAST_DATABASE_EVENT = event;
                    IphotoLoader.loadFromIPhoto(Thread.currentThread(), (IphotoDatasourceConfig) datasource, storeSession, timeLastUpdate, photoIds, iphotoAlbumIds);
                } else if (datasource.getType() == DatasourceType.Watchfolder && !Thread.currentThread().isInterrupted()) {
                    try {
                        myState = State.UpdatingTracksFromFolder;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningFolder");
                        MyTunesRssEventManager.getInstance().fireEvent(event);
                        MyTunesRss.LAST_DATABASE_EVENT = event;
                        FileSystemLoader.loadFromFileSystem(Thread.currentThread(), (WatchfolderDatasourceConfig) datasource, storeSession,
                                timeLastUpdate, trackIds, photoIds, m3uPlaylistIds);
                    } catch (ShutdownRequestedException e) {
                        // intentionally left blank
                    }
                }
            }
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + trackIds.size() + " tracks from database.");
            }
            storeSession.executeStatement(new RemoveTrackStatement(trackIds));
            DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + photoIds.size() + " photos from database.");
            }
            storeSession.executeStatement(new RemovePhotoStatement(photoIds));
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
        if (!iphotoAlbumIds.isEmpty() && !Thread.currentThread().isInterrupted()) {
            removeObsoletePhotoAlbums(storeSession, iphotoAlbumIds);
        }
        DatabaseBuilderCallable.doCheckpoint(storeSession, true);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Obsolete tracks and playlists removed from database.");
        }
        if (!Thread.currentThread().isInterrupted()) {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.buildingTrackIndex");
            MyTunesRssEventManager.getInstance().fireEvent(event);
            MyTunesRss.LAST_DATABASE_EVENT = event;
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

    private void removeObsoletePhotoAlbums(DataStoreSession storeSession, Collection<String> photoAlbumIds) throws SQLException {
        DeletePhotoAlbumStatement statement = new DeletePhotoAlbumStatement();
        for (String id : photoAlbumIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            DatabaseBuilderCallable.doCheckpoint(storeSession, false);
        }
    }

    public static State getState() {
        return myState;
    }
}
