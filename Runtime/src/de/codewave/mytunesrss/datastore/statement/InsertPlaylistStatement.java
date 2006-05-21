/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertPlaylistStatement
 */
public abstract class InsertPlaylistStatement implements DataStoreStatement {
    private String myId;
    private String myName;
    private PlaylistType myType;
    private List<String> myTrackIds;

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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO playlist VALUES ( ?, ?, ? )");
        statement.clearParameters();
        statement.setString(1, myId);
        statement.setString(2, myName);
        statement.setString(3, myType.name());
        statement.executeUpdate();
        statement = connection.prepareStatement("INSERT INTO link_track_playlist VALUES ( ?, ?, ? )");
        statement.setString(3, myId);
        int index = 0;
        for (Iterator<String> iterator = myTrackIds.iterator(); iterator.hasNext();) {
            statement.setInt(1, index++);
            statement.setString(2, iterator.next());
            statement.executeUpdate();
        }
    }
}