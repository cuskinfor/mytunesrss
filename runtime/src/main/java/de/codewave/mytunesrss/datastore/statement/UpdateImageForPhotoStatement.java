/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.UpdateImageForPhotoStatement
 */
public class UpdateImageForPhotoStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateImageForPhotoStatement.class);

    private String myPhotoId;
    private String myHash;
    private SmartStatement myStatement;

    public UpdateImageForPhotoStatement(String photoId, String hash) {
        myPhotoId = photoId;
        myHash = hash;
    }

    public void execute(Connection connection) throws SQLException {
        try {
            if (myStatement == null) {
                myStatement = MyTunesRssUtils.createStatement(connection, "updateImageForPhoto");
            }
            myStatement.clearParameters();
            myStatement.setString("photo_id", myPhotoId);
            myStatement.setString("hash", myHash);
            myStatement.setLong("updateTime", System.currentTimeMillis());
            myStatement.execute();
        } catch (SQLException e) {
            LOG.error("Could not update image for photo \"" + myPhotoId + "\".", e);
        }
    }

    public void clear() {
        myPhotoId = null;
        myHash = null;
    }
}
