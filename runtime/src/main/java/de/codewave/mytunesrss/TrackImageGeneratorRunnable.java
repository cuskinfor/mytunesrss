/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.HandlePhotoImagesStatement;
import de.codewave.mytunesrss.datastore.statement.HandleTrackImagesStatement;
import de.codewave.mytunesrss.datastore.statement.RecreateHelpTablesStatement;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.ResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class TrackImageGeneratorRunnable implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TrackImageGeneratorRunnable.class);

    private static class SimpleTrack {
        private String myId;
        private String myFile;
        private TrackSource mySource;

        private SimpleTrack(String id, String file, TrackSource source) {
            myId = id;
            myFile = file;
            mySource = source;
        }
    }

    public void run() {
        if (!MyTunesRss.CONFIG.isIgnoreArtwork()) {
            try {
                Collection<SimpleTrack> tracks = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Collection<SimpleTrack>>() {
                    @Override
                    public Collection<SimpleTrack> execute(Connection connection) throws SQLException {
                        return execute(MyTunesRssUtils.createStatement(connection, "getTracksWithMissingImages"), new ResultBuilder<SimpleTrack>() {
                            public SimpleTrack create(ResultSet resultSet) throws SQLException {
                                return new SimpleTrack(resultSet.getString("id"), resultSet.getString("file"), TrackSource.valueOf(resultSet.getString("source")));
                            }
                        }).getResults();
                    }
                });
                int count = 0;
                for (SimpleTrack track : tracks) {
                    if (Thread.interrupted()) {
                        break;
                    }
                    try {
                        MyTunesRss.STORE.executeStatement(new HandleTrackImagesStatement(track.mySource, new File(track.myFile), track.myId, 0));
                        count++;
                        if (count % 250 == 0) {
                            MyTunesRss.STORE.executeStatement(new DataStoreStatement() {
                                public void execute(Connection connection) throws SQLException {
                                    MyTunesRssUtils.createStatement(connection, "recreateHelpTablesAlbum").execute();
                                }
                            });
                        }
                    } catch (IOException e) {
                        log.error("Could not insert track image.", e);
                    }
                }
            } catch (SQLException e) {
                log.error("Could not fetch photos with missing thumbnails.", e);
            }
        }
    }
}
