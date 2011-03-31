/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.utils.sql.SmartStatementExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * de.codewave.mytunesrss.datastore.statement.SavePhotoAlbumStatement
 */
public class SavePhotoAlbumStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(SavePhotoAlbumStatement.class);

    protected String myId;
    private String myName;
    private List<String> myPhotoIds;
    private boolean myUpdate;

    protected String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public void setName(String name) {
        myName = name;
    }

    protected boolean isUpdate() {
        return myUpdate;
    }

    public void setUpdate(boolean update) {
        myUpdate = update;
    }

    public List<String> getPhotoIds() {
        return myPhotoIds;
    }

    public void setPhotoIds(List<String> photoIds) {
        myPhotoIds = photoIds;
    }

    public void execute(Connection connection) throws SQLException {
        if (myUpdate) {
            executeUpdate(connection);
        } else {
            executeInsert(connection);
        }
    }

    protected void executeInsert(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertPhotoAlbum");
        statement.setString("id", myId);
        statement.setString("name", myName);
        statement.setObject("photo_id", myPhotoIds);
        LOG.debug("Inserting photo album \"" + myName + "\" with " + myPhotoIds.size() + " photos.");
        statement.execute(new ExceptionHandler());
    }

    protected void executeUpdate(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePhotoAlbum");
        statement.setString("id", myId);
        statement.setString("name", myName);
        statement.setObject("photo_id", myPhotoIds);
        LOG.debug("Updating photo album \"" + myName + "\" with " + myPhotoIds.size() + " photos.");
        statement.execute(new ExceptionHandler());
    }

    public static class ExceptionHandler implements SmartStatementExceptionHandler {
        public void handleException(SQLException sqlException, boolean b) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not insert/update photo album", sqlException);
            }
        }
    }
}