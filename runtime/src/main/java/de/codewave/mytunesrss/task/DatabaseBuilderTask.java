/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.filesystem.*;
import de.codewave.mytunesrss.datastore.itunes.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.utils.sql.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * de.codewave.mytunesrss.task.DatabaseBuilderTaskk
 */
public class DatabaseBuilderTask extends MyTunesRssTask {
    public static void setLastSeenTime(DataStoreSession dataStoreSession, final Collection<String> trackIds) {
        if (!trackIds.isEmpty()) {
            try {
                dataStoreSession.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "setLastSeenTime");
                        statement.setItems("track_ids", trackIds);
                        statement.setLong("currentTime", System.currentTimeMillis());
                        statement.execute();
                    }
                });
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not set last-seen time.", e);
                }
            }
        }
    }

    public static void updateHelpTables(DataStoreSession session, int updatedCount) {
        if (updatedCount % MyTunesRssDataStore.UPDATE_HELP_TABLES_FREQUENCY == 0) {
            // recreate help tables every N tracks
            try {
                session.executeStatement(new RecreateHelpTablesStatement());
                session.commit();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not recreate help tables..", e);
                }
            }
        }
    }

    public enum State {
        UpdatingTracksFromItunes(), UpdatingTracksFromFolder(), UpdatingTrackImages(), Idle();
    }

    private static final Log LOG = LogFactory.getLog(DatabaseBuilderTask.class);
    private static Lock CURRENTLY_RUNNING = new ReentrantLock();
    private static State myState = State.Idle;
    private static final long MAX_TX_DURATION = 2500;
    private List<File> myDatasources = new ArrayList<File>();
    private boolean myExecuted;
    private static long TX_BEGIN;

    public DatabaseBuilderTask() {
        if (MyTunesRss.CONFIG.getDatasources() != null && MyTunesRss.CONFIG.getDatasources().length > 0) {
            for (String datasource : MyTunesRss.CONFIG.getDatasources()) {
                myDatasources.add(new File(datasource));
            }
        }
        if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getUploadDir())) {
            myDatasources.add(new File(MyTunesRss.CONFIG.getUploadDir().trim()));
        }
    }

    public boolean needsUpdate() throws SQLException {
        if (myDatasources != null) {
            for (File baseDir : myDatasources) {
                if (baseDir.isDirectory()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Database update needed.");
                    }
                    return true;
                } else if (baseDir.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(baseDir.getName()))) {
                    DataStoreSession session = MyTunesRss.STORE.getTransaction();
                    SystemInformation systemInformation;
                    try {
                        systemInformation = session.executeQuery(new GetSystemInformationQuery());
                    } finally {
                        session.commit();
                    }
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
        if (CURRENTLY_RUNNING.tryLock()) {
            try {
                MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                event.setMessageKey("settings.databaseUpdateRunning");
                MyTunesRssEventManager.getInstance().fireEvent(event);
                internalExecute();
                myExecuted = true;
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED);
            } catch (Exception e) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED_NOT_RUN);
                throw e;
            } finally {
                CURRENTLY_RUNNING.unlock();
            }
        } else {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED_NOT_RUN);
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
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting database update.");
            }
            final long timeUpdateStart = System.currentTimeMillis();
            SystemInformation systemInformation = storeSession.executeQuery(new GetSystemInformationQuery());
            runUpdate(systemInformation, storeSession);
            storeSession.commit();
            runImageUpdate(systemInformation, storeSession, timeUpdateStart);
            storeSession.executeStatement(new UpdateStatisticsStatement());
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE system_information SET lastupdate = " + timeUpdateStart);
                }
            });
            storeSession.commit();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating database checkpoint.");
                LOG.debug("Update took " + (System.currentTimeMillis() - timeUpdateStart) + " ms.");
            }
        } catch (Exception e) {
            storeSession.rollback();
            throw e;
        }
    }

    private void runImageUpdate(SystemInformation systemInformation, DataStoreSession storeSession, final long timeUpdateStart) throws SQLException {
        myState = State.UpdatingTrackImages;
        long timeLastUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : systemInformation.getLastUpdate();
        MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
        event.setMessageKey("settings.databaseUpdateRunningImages");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        TX_BEGIN = System.currentTimeMillis();
        DataStoreSession trackQuerySession = MyTunesRss.STORE.getTransaction();
        try {
            DataStoreQuery.QueryResult<Track> result = trackQuerySession.executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<Track>>() {
                public QueryResult<Track> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTracksForImageUpdate");
                    statement.setLong("timeUpdateStart", timeUpdateStart);
                    return execute(statement, new ResultBuilder<Track>() {
                        public Track create(ResultSet resultSet) throws SQLException {
                            Track track = new Track();
                            track.setId(resultSet.getString("id"));
                            track.setFile(new File(resultSet.getString("file")));
                            track.setLastImageUpdate(resultSet.getLong("last_image_update"));
                            return track;
                        }
                    });
                }
            });
            for (Track track = result.nextResult(); track != null; track = result.nextResult()) {
                if (track.getFile().lastModified() >= track.getLastImageUpdate()) {
                    storeSession.executeStatement(new HandleTrackImagesStatement(track.getFile(), track.getId()));
                }
                doCheckpoint(storeSession);
            }
        } finally {
            trackQuerySession.commit();
        }
    }

    public static void doCheckpoint(DataStoreSession storeSession) {
        long time = System.currentTimeMillis();
        if (TX_BEGIN == 0) {
            TX_BEGIN = time;
        }
        if (time - TX_BEGIN > MAX_TX_DURATION) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Committing transaction after " + (time - TX_BEGIN) + " milliseconds.");
            }
            storeSession.commit();
            TX_BEGIN = System.currentTimeMillis();
        }
    }

    private void runUpdate(SystemInformation systemInformation, DataStoreSession storeSession) throws SQLException, IOException {
        long timeLastUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : systemInformation.getLastUpdate();
        Collection<String> itunesPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunes.name()));
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
        if (myDatasources != null) {
            for (File datasource : myDatasources) {
                doCheckpoint(storeSession);
                if (datasource.isFile() && "xml".equalsIgnoreCase(FilenameUtils.getExtension(datasource.getName()))) {
                    myState = State.UpdatingTracksFromItunes;
                    MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                    event.setMessageKey("settings.databaseUpdateRunningItunes");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    ItunesLoader.loadFromITunes(datasource.toURL(), storeSession, timeLastUpdate, trackIds, itunesPlaylistIds);
                } else if (datasource.isDirectory()) {
                    myState = State.UpdatingTracksFromFolder;
                    MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                    event.setMessageKey("settings.databaseUpdateRunningFolder");
                    MyTunesRssEventManager.getInstance().fireEvent(event);
                    FileSystemLoader.loadFromFileSystem(datasource, storeSession, timeLastUpdate, trackIds, m3uPlaylistIds);
                }
            }
            // ensure the help tables are created with all the data
            storeSession.executeStatement(new RecreateHelpTablesStatement());
            storeSession.commit();

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing " + trackIds.size() + " tracks from database.");
        }
        storeSession.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTracks");
                statement.setItems("track_id", trackIds);
                statement.execute();
            }
        });
        storeSession.commit();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing " + (itunesPlaylistIds.size() + m3uPlaylistIds.size()) + " playlists from database.");
        }
        if (!itunesPlaylistIds.isEmpty()) {
            removeObsoletePlaylists(storeSession, itunesPlaylistIds);
        }
        if (!m3uPlaylistIds.isEmpty()) {
            removeObsoletePlaylists(storeSession, m3uPlaylistIds);
        }
        storeSession.commit();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Obsolete tracks and playlists removed from database.");
        }
    }

    private void removeObsoletePlaylists(DataStoreSession storeSession, Collection<String> databaseIds) throws SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Removing " + databaseIds.size() + " obsolete playlists.");
        }
        DeletePlaylistStatement statement = new DeletePlaylistStatement();
        for (String id : databaseIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            DatabaseBuilderTask.doCheckpoint(storeSession);
        }
        storeSession.commit();
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