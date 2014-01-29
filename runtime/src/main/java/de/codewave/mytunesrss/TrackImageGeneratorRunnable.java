/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.CommonTrackDatasourceConfig;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.datastore.statement.HandleTrackImagesStatement;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.utils.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackImageGeneratorRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackImageGeneratorRunnable.class);

    private static class SimpleTrack {
        private String myId;
        private String myFile;
        private TrackSource mySource;
        private String mySourceId;

        private SimpleTrack(String id, String file, TrackSource source, String sourceId) {
            myId = id;
            myFile = file;
            mySource = source;
            mySourceId = sourceId;
        }
    }

    private AtomicBoolean myTerminated = new AtomicBoolean(false);

    public synchronized void run() {
        try {
            final Set<String> sourceIds = new HashSet<>();
            for (DatasourceConfig datasourceConfig : MyTunesRss.CONFIG.getDatasources()) {
                if (datasourceConfig instanceof CommonTrackDatasourceConfig && ((CommonTrackDatasourceConfig) datasourceConfig).getTrackImageImportType() == ImageImportType.Auto) {
                    // only consider tracks from data sources which have the image import set to "AUTO"
                    sourceIds.add(datasourceConfig.getId());
                }
            }
            if (!sourceIds.isEmpty()) {
                try {
                    DataStoreQuery<Collection<SimpleTrack>> query = new DataStoreQuery<Collection<SimpleTrack>>() {
                        @Override
                        public Collection<SimpleTrack> execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getTracksWithMissingImages");
                            statement.setItems("sourceIds", sourceIds);
                            return execute(statement, new ResultBuilder<SimpleTrack>() {
                                public SimpleTrack create(ResultSet resultSet) throws SQLException {
                                    return new SimpleTrack(resultSet.getString("id"), resultSet.getString("file"), TrackSource.valueOf(resultSet.getString("source")), resultSet.getString("source_id"));
                                }
                            }).getNextResults(10000);
                        }
                    };
                    query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
                    Collection<SimpleTrack> tracks = MyTunesRss.STORE.executeQuery(query);
                    int count = 0;
                    Map<String, String> folderImageCache = new LinkedHashMap<String, String>(64, 0.75f, true) {
                        @Override
                        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                            return size() > 1000;
                        }
                    };
                    for (SimpleTrack track : tracks) {
                        if (Thread.interrupted()) {
                            break;
                        }
                        try {
                            MyTunesRss.STORE.executeStatement(new HandleTrackImagesStatement(folderImageCache, track.mySource, track.mySourceId, new File(track.myFile), track.myId, MyTunesRss.CONFIG.getDatasource(track.mySourceId).isUseSingleImageInFolder()));
                            count++;
                            if (count % 250 == 0) {
                                try {
                                    recreateAlbums();
                                } catch (SQLException e) {
                                    LOGGER.error("Could not recreate albums after inserting/updating images for 250 tracks.", e);
                                }
                            }
                        } catch (SQLException e) {
                            LOGGER.error("Could not insert/update images for \"" + track.myFile + "\".", e);
                        }
                    }
                    folderImageCache.clear();
                } catch (SQLException e) {
                    LOGGER.error("Could not fetch tracks with missing thumbnails.", e);
                } finally {
                    try {
                        recreateAlbums();
                    } catch (SQLException e) {
                        LOGGER.error("Could not recreate albums.", e);
                    }
                }
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Encountered unexpected exception. Caught to keep scheduled task alive.", e);
        } finally {
            myTerminated.set(true);
            notifyAll();
        }
    }

    private void recreateAlbums() throws SQLException {
        MyTunesRss.STORE.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                MyTunesRssUtils.createStatement(connection, "recreateHelpTablesAlbum").execute();
            }
        });
    }

    public synchronized void waitForTermination() {
        try {
            while (!myTerminated.get()) {
                wait();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for photo thumbnail generation termination.");
        }
    }
}
