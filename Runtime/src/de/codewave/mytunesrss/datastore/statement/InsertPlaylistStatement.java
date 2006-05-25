/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.datastore.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertPlaylistStatement
 */
public abstract class InsertPlaylistStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(InsertPlaylistStatement.class);
    
    private static final String SQL_PLAYLIST = "INSERT INTO playlist VALUES ( ?, ?, ? )";
    private static final String SQL_LINK = "INSERT INTO link_track_playlist VALUES ( ?, ?, ? )";

    private PreparedStatement myInsertPlaylistStatement;
    private PreparedStatement myInsertLinkStatement;
    private String myId;
    private String myName;
    private PlaylistType myType;
    private List<String> myTrackIds;

    protected InsertPlaylistStatement() {
        // intentionally left blank
    }

    protected InsertPlaylistStatement(DataStoreSession storeSession) throws SQLException {
        myInsertPlaylistStatement = storeSession.prepare(SQL_PLAYLIST);
        myInsertLinkStatement = storeSession.prepare(SQL_LINK);
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

    public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = myInsertPlaylistStatement != null ? myInsertPlaylistStatement : connection.prepareStatement(SQL_PLAYLIST);
        statement.clearParameters();
        statement.setString(1, myId);
        statement.setString(2, myName);
        statement.setString(3, myType.name());
        statement.execute();
        statement = myInsertLinkStatement != null ? myInsertLinkStatement : connection.prepareStatement(SQL_LINK);
        statement.setString(3, myId);
        int index = 0;
        for (Iterator<String> iterator = myTrackIds.iterator(); iterator.hasNext();) {
            statement.setInt(1, index++);
            statement.setString(2, iterator.next());
            try {
                statement.execute();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create link from playlist \"" + myName + "\" to track with id " + myId, e);
                }
            }
        }
    }
}