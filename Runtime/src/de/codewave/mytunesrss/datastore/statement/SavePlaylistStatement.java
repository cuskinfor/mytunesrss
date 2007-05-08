/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement
 */
public abstract class SavePlaylistStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(SavePlaylistStatement.class);

    protected String myId;
    private String myName;
    private PlaylistType myType;
    private List<String> myTrackIds;

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

    protected void executeInsert(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertPlaylist");
        statement.setString("id", myId);
        statement.setString("name", myName);
        statement.setString("type", myType.name());
        statement.setObject("track_id", myTrackIds);
        statement.execute(new ExceptionHandler());
    }

    protected void executeUpdate(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertPlaylist");
        statement.setString("id", myId);
        statement.setString("name", myName);
        statement.setObject("track_id", myTrackIds);
        statement.execute(new ExceptionHandler());
    }

    public static class ExceptionHandler implements SmartStatementExceptionHandler {
        public void handleException(SQLException sqlException, boolean b) {
            // intentionally left blank
        }
    }
}