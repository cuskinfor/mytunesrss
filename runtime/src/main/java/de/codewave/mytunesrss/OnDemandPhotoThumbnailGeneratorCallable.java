/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.HandlePhotoImagesStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class OnDemandPhotoThumbnailGeneratorCallable implements Callable<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnDemandPhotoThumbnailGeneratorCallable.class);

    private String myId;

    private File myFile;

    public OnDemandPhotoThumbnailGeneratorCallable(String id, File file) {
        myId = id;
        myFile = file;
    }

    public String call() throws SQLException {
        HandlePhotoImagesStatement statement = new HandlePhotoImagesStatement(myFile, myId);
        MyTunesRss.STORE.executeStatement(statement);
        return statement.getImageHash();
    }

}
