/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.*;
import de.codewave.mytunesrss.datastore.filesystem.FileSystemLoader;
import de.codewave.mytunesrss.datastore.iphoto.ApertureLoader;
import de.codewave.mytunesrss.datastore.iphoto.IphotoLoader;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.datastore.itunes.MissingItunesFiles;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.datastore.updatequeue.*;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.utils.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderCallable implements Callable<Boolean> {

    public enum State {
        UpdatingTracksFromItunes(), UpdatingTracksFromFolder(), UpdatingTrackImages(), Idle(), UpdatingTracksFromIphoto(), UpdatingTracksFromAperture();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseBuilderCallable.class);

    private static State myState = State.Idle;

    private List<DatasourceConfig> myDatasources = new ArrayList<DatasourceConfig>();

    private boolean myIgnoreTimestamps;

    protected DatabaseUpdateQueue myQueue = new DatabaseUpdateQueue(2500);

    public DatabaseBuilderCallable(Collection<DatasourceConfig> dataSources, boolean ignoreTimestamps) {
        myIgnoreTimestamps = ignoreTimestamps;
        for (DatasourceConfig datasource : dataSources) {
            addToDatasources(datasource);
        }
    }

    private void addToDatasources(DatasourceConfig datasource) {
        File file = new File(datasource.getDefinition());
        if (datasource.getType() == DatasourceType.Watchfolder && file.exists()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding folder \"" + file.getAbsolutePath() + "\" to database update sources.");
            }
            myDatasources.add(datasource);
        } else if (datasource.getType() == DatasourceType.Itunes && file.exists()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding iTunes XML file \"" + file.getAbsolutePath() + "\" to database update sources.");
            }
            myDatasources.add(datasource);
        } else if (datasource.getType() == DatasourceType.Iphoto && new File(file, IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME).isFile()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding iPhoto XML file \"" + new File(file, IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME).getAbsolutePath() + "\" to database update sources.");
            }
            myDatasources.add(datasource);
        } else if (datasource.getType() == DatasourceType.Aperture && new File(file, ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).isFile()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Adding Aperture XML file \"" + new File(file, ApertureDatasourceConfig.APERTURE_XML_FILE_NAME).getAbsolutePath() + "\" to database update sources.");
            }
            myDatasources.add(datasource);
        }
    }

    public Boolean call() throws Exception {
        myQueue.start();
        Boolean result = Boolean.FALSE;
        MyTunesRss.EXECUTOR_SERVICE.cancelImageGenerators();
        try {
            MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunning");
            myQueue.offer(new MyTunesRssEventEvent(event));
            internalExecute();
            result = Boolean.TRUE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception during import.", e);
            }
        } finally {
            if (Thread.interrupted()) { // clear interrupt status here to prevent interrupted exception in offer call
                LOGGER.info("Database update cancelled.");
            }
            myQueue.offer(new TerminateEvent());
            myQueue.waitForTermination();
            MyTunesRss.EXECUTOR_SERVICE.scheduleImageGenerators();
        }
        return result;
    }

    public void internalExecute() throws Exception {
        try {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Starting database update.");
            }
            final long timeUpdateStart = System.currentTimeMillis();
            final Map<String, MissingItunesFiles> missingItunesFiles = runUpdate();
            if (!Thread.currentThread().isInterrupted()) {
                if (myDatasources != null) {
                    for (DatasourceConfig datasourceConfig : myDatasources) {
                        MyTunesRss.CONFIG.getDatasource(datasourceConfig.getId()).setLastUpdate(timeUpdateStart);
                    }
                    MyTunesRss.CONFIG.save();
                }
                myQueue.offer(new CommittingDataStoreStatementEvent(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        Statement statement = connection.createStatement();
                        try {
                            statement.execute("UPDATE system_information SET lastupdate = " + timeUpdateStart);
                        } finally {
                            statement.close();
                        }
                    }
                }, false));
            }
            if (!Thread.currentThread().isInterrupted()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Update took " + (System.currentTimeMillis() - timeUpdateStart) + " ms.");
                }
                myQueue.offer(new DataStoreEvent() {
                    public boolean execute(DataStoreSession session) {
                        try {
                            MyTunesRss.ADMIN_NOTIFY.notifyDatabaseUpdate((System.currentTimeMillis() - timeUpdateStart), missingItunesFiles, MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery()));
                        } catch (SQLException e) {
                            LOGGER.warn("Could not notify admin of finished database update.", e);
                        }
                        return true;
                    }

                    public boolean isCheckpointRelevant() {
                        return false;
                    }
                });
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @return Map with the number of missing files per iTunes XML.
     * @throws SQLException
     * @throws IOException
     */
    private Map<String, MissingItunesFiles> runUpdate() throws SQLException, IOException, InterruptedException {
        Map<String, MissingItunesFiles> missingItunesFiles = new HashMap<String, MissingItunesFiles>();
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
        if (myDatasources != null && !Thread.currentThread().isInterrupted()) {
            long lastDatabaseUpdate = 0;
            try {
                lastDatabaseUpdate = MyTunesRss.STORE.executeQuery(new GetSystemInformationQuery()).getLastUpdate();
            } catch (SQLException e) {
                LOGGER.warn("Could not get last database update for defaulting missing values of data sources.", e);
            }
            try {
                for (DatasourceConfig datasource : myDatasources) {
                    long lastUpdate = myIgnoreTimestamps ? Long.MIN_VALUE : (datasource.getLastUpdate() == 0 ? lastDatabaseUpdate : datasource.getLastUpdate());
                    if (datasource.getType() == DatasourceType.Itunes && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromItunes;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningItunes");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        missingItunesFiles.put(new File(datasource.getDefinition()).getCanonicalPath(), ItunesLoader.loadFromITunes(Thread.currentThread(), (ItunesDatasourceConfig) datasource, myQueue, lastUpdate, trackIds));
                    } else if (datasource.getType() == DatasourceType.Iphoto && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromIphoto;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningIphoto");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        IphotoLoader.loadFromIPhoto(Thread.currentThread(), (IphotoDatasourceConfig) datasource, myQueue, lastUpdate, photoIds);
                    } else if (datasource.getType() == DatasourceType.Aperture && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromAperture;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningAperture");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        ApertureLoader.loadFromAperture(Thread.currentThread(), (ApertureDatasourceConfig) datasource, myQueue, lastUpdate, photoIds);
                    } else if (datasource.getType() == DatasourceType.Watchfolder && !Thread.currentThread().isInterrupted()) {
                        myState = State.UpdatingTracksFromFolder;
                        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRunningFolder");
                        myQueue.offer(new MyTunesRssEventEvent(event));
                        FileSystemLoader.loadFromFileSystem(Thread.currentThread(), (WatchfolderDatasourceConfig) datasource, myQueue, lastUpdate, trackIds, photoIds);
                    }
                }
            } catch (ShutdownRequestedException e) {
                // intentionally left blank
            }
        }
        final Collection<String> updatedDataSourceIds = MyTunesRssUtils.toDatasourceIds(myDatasources);
        if (!Thread.currentThread().isInterrupted()) {
            // Add all removed data sources to the list of updated ones, so all tracks, photos, etc. from those data sources is removed now
            Set<String> dataSourceIdsFromDatabase = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Set<String>>() {
                public Set<String> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getDataSourceIds");
                    ResultSet rs = statement.executeQuery();
                    Set<String> ids = new HashSet<String>();
                    while (rs.next()) {
                        ids.add(rs.getString("source_id"));
                    }
                    return ids;
                }
            });
            for (String dataSourceId : dataSourceIdsFromDatabase) {
                boolean found = false;
                for (DatasourceConfig datasourceConfig : MyTunesRss.CONFIG.getDatasources()) {
                    if (datasourceConfig.getId().equals(dataSourceId)) {
                        found = true;
                    }
                }
                if (!found && !updatedDataSourceIds.contains(dataSourceId)) {
                    updatedDataSourceIds.add(dataSourceId);
                }
            }
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + trackIds.size() + " tracks from database.");
            }
            myQueue.offer(new DataStoreStatementEvent(new RemoveTrackStatement(trackIds, updatedDataSourceIds), true));
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Trying to remove up to " + photoIds.size() + " photos from database.");
            }
            myQueue.offer(new DataStoreStatementEvent(new RemovePhotoStatement(photoIds, updatedDataSourceIds), true));
        }
        if (!Thread.currentThread().isInterrupted()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Removing outdated playlists and photo albums.");
            }
            myQueue.offer(new DataStoreStatementEvent(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "cleanupPlaylistsAndPhotoAlbumsAfterUpdate");
                    statement.setItems("source_id", updatedDataSourceIds);
                    statement.execute();
                }
            }, true));
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Obsolete tracks, photos and playlists removed from database.");
            }
        }
        return missingItunesFiles;
    }

    public static State getState() {
        return myState;
    }
}
