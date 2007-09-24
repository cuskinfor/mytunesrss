/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.filesystem.*;
import de.codewave.mytunesrss.datastore.itunes.*;
import de.codewave.mytunesrss.datastore.statement.*;
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
            runImageUpdate(systemInformation, storeSession);
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE system_information SET lastupdate = " + timeUpdateStart);
                }
            });
            storeSession.commit();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating database checkpoint.");
            }
        } catch (Exception e) {
            storeSession.rollback();
            throw e;
        }
    }

    private void runImageUpdate(SystemInformation systemInformation, DataStoreSession storeSession) throws SQLException {
        myState = State.UpdatingTrackImages;
        MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
        event.setMessageKey("settings.databaseUpdateRunningImages");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        int startIndex = 0;
        Collection<Track> tracks = null;
        TX_BEGIN = System.currentTimeMillis();
        do {
            final int localStartIndex = startIndex;
            tracks = storeSession.executeQuery(new DataStoreQuery<Collection<Track>>() {
                public Collection<Track> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findTracksInRange");
                    statement.setInt("startIndex", localStartIndex);
                    statement.setInt("selectCount", 25);
                    ResultSet resultSet = statement.executeQuery();
                    Collection<Track> tracks = new ArrayList<Track>();
                    while (resultSet.next()) {
                        Track track = new Track();
                        track.setId(resultSet.getString("id"));
                        track.setFile(new File(resultSet.getString("file")));
                        tracks.add(track);
                    }
                    return tracks;
                }
            });
            for (Track track : tracks) {
                if (track.getFile().lastModified() >= systemInformation.getLastUpdate()) {
                    storeSession.executeStatement(new HandleTrackImagesStatement(track.getFile(), track.getId()));
                }
                doCheckpoint(storeSession);
            }
            startIndex += tracks.size();
        } while (tracks != null && tracks.size() > 0);
    }

    public static void doCheckpoint(DataStoreSession storeSession) {
        long time = System.currentTimeMillis();
        if (time - TX_BEGIN > MAX_TX_DURATION) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Committing transaction after " + (time - TX_BEGIN) + " milliseconds.");
                }
                storeSession.commit();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not commit transaction.", e);
                }
            }
            TX_BEGIN = System.currentTimeMillis();
        }
    }

    private void runUpdate(SystemInformation systemInformation, DataStoreSession storeSession) throws SQLException, IOException {
        long timeLastUpdate = MyTunesRss.CONFIG.isIgnoreTimestamps() ? Long.MIN_VALUE : systemInformation.getLastUpdate();
        Collection<String> trackIds = storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.ITunes.name()));
        Collection<String> itunesPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.ITunes.name()));
        Collection<String> m3uPlaylistIds = storeSession.executeQuery(new FindPlaylistIdsQuery(PlaylistType.M3uFile.name()));
        trackIds.addAll(storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.FileSystem.name())));
        if (myDatasources != null) {
            for (File datasource : myDatasources) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Parsing \"" + datasource.getAbsolutePath() + "\".");
                }
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
        }
        if (!trackIds.isEmpty()) {
            removeObsoleteTracks(storeSession, trackIds);
        }
        if (!itunesPlaylistIds.isEmpty()) {
            removeObsoletePlaylists(storeSession, itunesPlaylistIds);
        }
        if (!m3uPlaylistIds.isEmpty()) {
            removeObsoletePlaylists(storeSession, m3uPlaylistIds);
        }
        storeSession.commit();
    }

    private static void removeObsoleteTracks(DataStoreSession storeSession, Collection<String> databaseIds) throws SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Removing " + databaseIds.size() + " obsolete tracks.");
        }
        int count = 0;
        DeleteTrackStatement statement = new DeleteTrackStatement();
        for (String id : databaseIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            count++;
            if (count == 500) {
                count = 0;
                storeSession.commit();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Committing transaction after 500 deleted tracks.");
                }
            }
        }
        storeSession.commit();
    }

    private static void removeObsoletePlaylists(DataStoreSession storeSession, Collection<String> databaseIds) throws SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Removing " + databaseIds.size() + " obsolete playlists.");
        }
        int count = 0;
        DeletePlaylistStatement statement = new DeletePlaylistStatement();
        for (String id : databaseIds) {
            statement.setId(id);
            storeSession.executeStatement(statement);
            count++;
            if (count == 500) {
                count = 0;
                storeSession.commit();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Committing transaction after 500 deleted playlists.");
                }
            }
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