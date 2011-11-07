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
import de.codewave.mytunesrss.datastore.updatequeue.*;
import de.codewave.utils.sql.*;
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

    public static void updateHelpTables(DatabaseUpdateQueue queue, int updatedCount) {
        if (updatedCount % MyTunesRssDataStore.UPDATE_HELP_TABLES_FREQUENCY == 0) {
            queue.offer(new DataStoreStatementEvent(new RecreateHelpTablesStatement()));
        }
    }

    public enum State {
        UpdatingTracksFromItunes(), UpdatingTracksFromFolder(), UpdatingTrackImages(), Idle(), UpdatingTracksFromIphoto();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBuilderCallable.class);

    private static State myState = State.Idle;

    private List<DatasourceConfig> myFileDatasources = new ArrayList<DatasourceConfig>();

    private boolean myIgnoreTimestamps;

    protected DatabaseUpdateQueue myQueue = new DatabaseUpdateQueue(2500);

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
                            LOGGER.info("Not adding \"" + file.getAbsolutePath() + "\" to database update sources.");
                            return; // new dir is already scanned through other dir
                        } else if (de.codewave.utils.io.IOUtils.isContained(file, eachFile)) {
                            // existing one will be scanned by adding new one, so remove existing one
                            LOGGER.info("Removing folder \"" + eachFile.getAbsolutePath() + "\" from database update sources.");
                            iter.remove();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Could not check whether or not folder may be added, so adding it.", e);
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

    public Boolean call() throws Exception {
        Boolean result = Boolean.FALSE;
        try {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunning");
            myQueue.offer(new MyTunesRssEventEvent(event));
            internalExecute();
            result = Boolean.TRUE;
            if (!Thread.currentThread().isInterrupted()) {
                myQueue.offer(new DataStoreStatementEvent(new RefreshSmartPlaylistsStatement()));
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception during import.", e);
            }
        } finally {
            myQueue.offer(new MyTunesRssEventEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED)));
            myQueue.offer(new TerminateEvent());
        }
        return result;
    }

    public void internalExecute() throws Exception {
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting database update.");
            }
            final long timeUpdateStart = System.currentTimeMillis();
            SystemInformation systemInformation = MyTunesRss.STORE.getTransaction().executeQuery(new GetSystemInformationQuery());
            final Map<String, Long> missingItunesFiles = runUpdate(systemInformation);
            if (!Thread.currentThread().isInterrupted()) {
                myQueue.offer(new DataStoreStatementEvent(new UpdateStatisticsStatement()));
                myQueue.offer(new DataStoreStatementEvent(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        connection.createStatement().execute(
                                "UPDATE system_information SET lastupdate = " + timeUpdateStart);
                    }
                }));
            }
            if (!MyTunesRss.CONFIG.isIgnoreArtwork() && !Thread.currentThread().isInterrupted()) {
                runImageUpdate(timeUpdateStart);
            }
            updateHelpTables(myQueue, 0); // update image references for albums
            if (!Thread.currentThread().isInterrupted()) {
                deleteOrphanedImages();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Update took " + (System.currentTimeMillis() - timeUpdateStart) + " ms.");
                }
                myQueue.offer(new DataStoreEvent() {
                    public boolean execute(DataStoreSession session) {
                        try {
                            MyTunesRss.ADMIN_NOTIFY.notifyDatabaseUpdate((System.currentTimeMillis() - timeUpdateStart), missingItunesFiles, MyTunesRss.STORE.getTransaction().executeQuery(new GetSystemInformationQuery()));
                        } catch (SQLException e) {
                            LOGGER.warn("Could not notify admin of finished database update.", e);
                        }
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            throw e;
        }
    }

    protected void deleteOrphanedImages() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Deleting orphaned images.");
        }
        myQueue.offer(new DataStoreStatementEvent(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                MyTunesRssUtils.createStatement(connection, "deleteOrphanedImages").execute();
            }
        }));
    }

    protected void runImageUpdate(final long timeUpdateStart) {
        myState = State.UpdatingTrackImages;
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningImages");
        myQueue.offer(new MyTunesRssEventEvent(event));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Processing track images.");
        }
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            DataStoreQuery.QueryResult<Track> result = tx.executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<Track>>() {
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
                myQueue.offer(new DataStoreStatementEvent(new HandleTrackImagesStatement(track.getSource(), track.getFile(), track
                        .getId(), timeLastImageUpdate, track.getMediaType() == MediaType.Image)));
            }
        } catch (SQLException e) {
            LOGGER.error("Could not find tracks for image update.", e);
        } finally {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Finished processing track images.");
            }
            tx.rollback();
        }
    }

    /**
     * @param systemInformation
     * @return Map with the number of missing files per iTunes XML.
     * @throws SQLException
     * @throws IOException
     */
    private Map<String, Long> runUpdate(SystemInformation systemInformation)
            throws SQLException, IOException {
        Map<String, Long> missingItunesFiles = new HashMap<String, Long>();
        long timeLastUpdate = myIgnoreTimestamps ? Long.MIN_VALUE : systemInformation
                .getLastUpdate();
        Collection<String> itunesPlaylistIds = MyTunesRss.STORE.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunes.name()));
        Collection<String> photoAlbumIds = MyTunesRss.STORE.executeQuery(new FindPhotoAlbumIdsQuery());
        itunesPlaylistIds.addAll(MyTunesRss.STORE.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunesFolder.name())));
        Collection<String> m3uPlaylistIds = MyTunesRss.STORE.executeQuery(new FindPlaylistIdsQuery(PlaylistType.M3uFile.name()));
        final Set<String> trackIds = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Set<String>>() {
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
        final Set<String> photoIds = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Set<String>>() {
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
            try {
                for (DatasourceConfig datasource : myFileDatasources) {
                    if (datasource.getType() == DatasourceType.Itunes && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromItunes;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningItunes");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        missingItunesFiles.put(new File(datasource.getDefinition()).getCanonicalPath(), ItunesLoader.loadFromITunes(Thread
                                .currentThread(), (ItunesDatasourceConfig) datasource, myQueue, timeLastUpdate, trackIds,
                                itunesPlaylistIds));
                    } else if (datasource.getType() == DatasourceType.Iphoto && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromIphoto;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningIphoto");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        IphotoLoader.loadFromIPhoto(Thread.currentThread(), (IphotoDatasourceConfig) datasource, myQueue, timeLastUpdate, photoIds, photoAlbumIds);
                    } else if (datasource.getType() == DatasourceType.Watchfolder && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromFolder;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningFolder");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        FileSystemLoader.loadFromFileSystem(Thread.currentThread(), (WatchfolderDatasourceConfig) datasource, myQueue,
                                timeLastUpdate, trackIds, photoIds, m3uPlaylistIds, photoAlbumIds);
                    }
                }
            } catch (ShutdownRequestedException e) {
                // intentionally left blank
            }
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + trackIds.size() + " tracks from database.");
            }
            myQueue.offer(new DataStoreStatementEvent(new RemoveTrackStatement(trackIds)));
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + photoIds.size() + " photos from database.");
            }
            myQueue.offer(new DataStoreStatementEvent(new RemovePhotoStatement(photoIds)));
        }
        if (!Thread.currentThread().isInterrupted()) {
            // ensure the help tables are created with all the data
            updateHelpTables(myQueue, 0);
            myQueue.offer(new DataStoreStatementEvent(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    MyTunesRssUtils.createStatement(connection, "removeObsoletePhotoAlbumsAndPlaylists").execute();
                }
            }));
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Obsolete tracks and playlists removed from database.");
        }
        if (!Thread.currentThread().isInterrupted()) {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.buildingTrackIndex");
            myQueue.offer(new MyTunesRssEventEvent(event));
            MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        }
        return missingItunesFiles;
    }

    public static State getState() {
        return myState;
    }
}
