/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.utils.sql.SmartStatementExceptionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement
 */
public abstract class SavePlaylistStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(SavePlaylistStatement.class);

    protected String myId;
    private String myName;
    private PlaylistType myType;
    private List<String> myTrackIds;
    private boolean myUpdate;
    private String myUserName;
    private boolean myUserPrivate;

    protected SavePlaylistStatement() {
        // intentionally left blank
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

    public void setUpdate(boolean update) {
        myUpdate = update;
    }

    public void setUserName(String userName) {
        myUserName = userName;
    }

    public void setUserPrivate(boolean userPrivate) {
        myUserPrivate = userPrivate;
    }

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
        statement.setString("name", myName);
        statement.setString("type", myType.name());
        statement.setObject("track_id", myTrackIds);
        statement.setBoolean("user_private", myUserPrivate);
        statement.setString("user_name", myUserName);
        statement.setLong("ts", System.currentTimeMillis());
        statement.execute(new ExceptionHandler());
    }

    protected void executeUpdate(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePlaylist");
        statement.setString("id", myId);
        statement.setString("name", myName);
        statement.setObject("track_id", myTrackIds);
        statement.setBoolean("user_private", myUserPrivate);
        statement.setString("user_name", myUserName);
        statement.execute(new ExceptionHandler());
    }

    public static class ExceptionHandler implements SmartStatementExceptionHandler {
        public void handleException(SQLException sqlException, boolean b) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not insert/update playlist", sqlException);
            }
        }
    }
}