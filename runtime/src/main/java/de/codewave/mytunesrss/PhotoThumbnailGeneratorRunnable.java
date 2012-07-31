/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.HandlePhotoImagesStatement;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class PhotoThumbnailGeneratorRunnable implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PhotoThumbnailGeneratorRunnable.class);

    private static class SimplePhoto {
        private String myId;
        private String myFile;

        private SimplePhoto(String id, String file) {
            myId = id;
            myFile = file;
        }
    }

    public void run() {
        if (MyTunesRss.CONFIG.getPhotoThumbnailImportType() == ImageImportType.Auto) {
            try {
                Collection<SimplePhoto> photos = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Collection<SimplePhoto>>() {
                    @Override
                    public Collection<SimplePhoto> execute(Connection connection) throws SQLException {
                        return execute(MyTunesRssUtils.createStatement(connection, "getPhotosWithMissingThumbnails"), new ResultBuilder<SimplePhoto>() {
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
                    MyTunesRss.STORE.executeStatement(new HandlePhotoImagesStatement(new File(photo.myFile), photo.myId));
                }
            } catch (SQLException e) {
                log.error("Could not fetch photos with missing thumbnails.", e);
            }
        }
    }
}
