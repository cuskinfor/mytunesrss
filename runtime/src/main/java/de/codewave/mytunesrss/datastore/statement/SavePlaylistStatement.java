/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
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
 * de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement
 */
public abstract class SavePlaylistStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(SavePlaylistStatement.class);

    protected String myId;
    private String myName;
    private PlaylistType myType;
    private List<String> myTrackIds;
    private boolean myUpdate;
    private String myUserName;
    private boolean myUserPrivate;
    private String myContainerId;
    private String mySourceId;

    protected SavePlaylistStatement(String sourceId) {
        mySourceId = sourceId;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }

    public void setName(String name) {
        myName = name;
    }

    protected void setType(PlaylistType type) {
        myType = type;
    }

    public void setTrackIds(List<String> trackIds) {
        myTrackIds = trackIds;
    }

    protected boolean isUpdate() {
        return myUpdate;
    }

    public void setUpdate(boolean update) {
        myUpdate = update;
    }

    public void setUserName(String userName) {
        myUserName = userName;
    }

    public void setUserPrivate(boolean userPrivate) {
        myUserPrivate = userPrivate;
    }

    public String getContainerId() {
        return myContainerId;
    }

    public void setContainerId(String containerId) {
        myContainerId = containerId;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        if (myUpdate) {
            executeUpdate(connection);
        } else {
            executeInsert(connection);
        }
    }

    protected void executeInsert(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertPlaylist");
        statement.setString("id", myId);
        statement.setString("source_id", mySourceId);
        statement.setString("name", myName);
        statement.setString("type", myType.name());
        statement.setObject("track_id", myTrackIds);
        statement.setBoolean("user_private", myUserPrivate);
        statement.setString("user_name", myUserName);
        statement.setString("container_id", myContainerId);
        statement.setLong("ts", System.currentTimeMillis());
        statement.execute(new ExceptionHandler());
    }

    protected void executeUpdate(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePlaylist");
        statement.setString("id", myId);
        statement.setString("source_id", mySourceId);
        statement.setString("name", myName);
        statement.setObject("track_id", myTrackIds);
        statement.setBoolean("user_private", myUserPrivate);
        statement.setString("user_name", myUserName);
        statement.setString("container_id", myContainerId);
        statement.execute(new ExceptionHandler());
    }

    public static class ExceptionHandler implements SmartStatementExceptionHandler {
        @Override
        public void handleException(SQLException sqlException, boolean b) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not insert/update playlist", sqlException);
            }
        }
    }
}