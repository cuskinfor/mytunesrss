/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.datastore.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement
 */
public abstract class SavePlaylistStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(SavePlaylistStatement.class);

    private static final String SQL_PLAYLIST = "INSERT INTO playlist VALUES ( ?, ?, ?, ? )";
    private static final String SQL_UPDATE_PLAYLIST = "UPDATE playlist SET name = ? WHERE id = ?";
    private static final String SQL_LINK = "INSERT INTO link_track_playlist VALUES ( ?, ?, ? )";
    private static final String SQL_UPDATE_TRACK_COUNT = "UPDATE playlist SET track_count = ? WHERE id = ?";
    private static final String SQL_DELETE_LINKS = "DELETE FROM link_track_playlist WHERE playlist_id = ?";

    private PreparedStatement myInsertPlaylistStatement;
    private PreparedStatement myInsertLinkStatement;
    private PreparedStatement myUpdateTrackCountStatement;
    protected String myId;
    private String myName;
    private PlaylistType myType;
    private List<String> myTrackIds;

    protected SavePlaylistStatement() {
        // intentionally left blank
    }

    protected SavePlaylistStatement(DataStoreSession storeSession) throws SQLException {
        myInsertPlaylistStatement = storeSession.prepare(SQL_PLAYLIST);
        myInsertLinkStatement = storeSession.prepare(SQL_LINK);
        myUpdateTrackCountStatement = storeSession.prepare(SQL_UPDATE_TRACK_COUNT);
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
        PreparedStatement statement = myInsertPlaylistStatement != null ? myInsertPlaylistStatement : connection.prepareStatement(SQL_PLAYLIST);
        statement.clearParameters();
        statement.setString(1, myId);
        statement.setString(2, myName);
        statement.setString(3, myType.name());
        statement.setInt(4, 0);
        statement.execute();
        createLinksAndUpdateCount(connection);
    }

    private void createLinksAndUpdateCount(Connection connection) throws SQLException {
        PreparedStatement statement;
        statement = myInsertLinkStatement != null ? myInsertLinkStatement : connection.prepareStatement(SQL_LINK);
        statement.setString(3, myId);
        int index = 0;
        int inserted = 0;
        for (Iterator<String> iterator = myTrackIds.iterator(); iterator.hasNext();) {
            statement.setInt(1, index++);
            statement.setString(2, iterator.next());
            try {
                statement.execute();
                inserted++;
            } catch (SQLException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not create link from playlist \"" + myName + "\" to track with id " + myId);
                }
            }
        }
        statement = myUpdateTrackCountStatement != null ? myUpdateTrackCountStatement : connection.prepareStatement(SQL_UPDATE_TRACK_COUNT);
        statement.setInt(1, inserted);
        statement.setString(2, myId);
        statement.execute();
    }

    protected void executeUpdate(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PLAYLIST);
        statement.setString(1, myName);
        statement.setString(2, myId);
        statement.execute();
        statement = connection.prepareStatement(SQL_DELETE_LINKS);
        statement.setString(1, myId);
        statement.execute();
        createLinksAndUpdateCount(connection);
    }
}