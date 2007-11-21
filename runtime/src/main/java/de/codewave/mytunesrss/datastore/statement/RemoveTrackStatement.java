/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.RemoveTrackStatement
 */
public class RemoveTrackStatement implements DataStoreStatement {
    private Collection<String> myTrackIds;

    public RemoveTrackStatement(Collection<String> trackIds) {
        myTrackIds = trackIds;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTrack");
        int count = 0;
        for (String trackId : myTrackIds) {
            statement.setString("track_id", trackId);
            statement.execute();
            if (count++ % 500 == 0) {
                connection.commit();
            }
        }
    }
}