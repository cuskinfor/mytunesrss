/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.CommonPhotoDatasourceConfig;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotoThumbnailGeneratorRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoThumbnailGeneratorRunnable.class);

    private static class SimplePhoto {
        private String myId;
        private String myFile;

        private SimplePhoto(String id, String file) {
            myId = id;
            myFile = file;
        }
    }

    private AtomicBoolean myTerminated = new AtomicBoolean(false);

    public synchronized void run() {
        try {
            final Set<String> sourceIds = new HashSet<>();
            for (DatasourceConfig datasourceConfig : MyTunesRss.CONFIG.getDatasources()) {
                if (datasourceConfig instanceof CommonPhotoDatasourceConfig && ((CommonPhotoDatasourceConfig) datasourceConfig).getPhotoThumbnailImportType() == ImageImportType.Auto) {
                    // only consider photos from data sources which have the thumbnail import set to "AUTO"
                    sourceIds.add(datasourceConfig.getId());
                }
            }
            if (!sourceIds.isEmpty()) {
                try {
                    Collection<SimplePhoto> photos = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Collection<SimplePhoto>>() {
                        @Override
                        public Collection<SimplePhoto> execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhotosWithMissingThumbnails");
                            statement.setItems("sourceIds", sourceIds);
                            return execute(statement, new ResultBuilder<SimplePhoto>() {
                                public SimplePhoto create(ResultSet resultSet) throws SQLException {
                                    return new SimplePhoto(resultSet.getString("id"), resultSet.getString("file"));
                                }
                            }).getResults();
                        }
                    });
                    for (SimplePhoto photo : photos) {
                        if (Thread.interrupted()) {
                            break;
                        }
                        new OnDemandPhotoThumbnailGeneratorCallable(photo.myId, new File(photo.myFile)).call();
                    }
                } catch (SQLException e) {
                    LOGGER.error("Could not fetch photos with missing thumbnails.", e);
                }
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Encountered unexpected exception. Caught to keep scheduled task alive.", e);
        } finally {
            myTerminated.set(true);
            notifyAll();
        }
    }

    public synchronized void waitForTermination() {
        try {
            while (!myTerminated.get()) {
                wait();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for photo thumbnail generation termination.", e);
        }
    }
}
