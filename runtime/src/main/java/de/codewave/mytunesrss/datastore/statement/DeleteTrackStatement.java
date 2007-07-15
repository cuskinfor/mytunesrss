/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.MyTunesRssUtils;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.DeleteTrackStatement
 */
public class DeleteTrackStatement implements DataStoreStatement {
    private String myId;

    public void setId(String id) {
        myId = id;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "deleteTrackById");
        statement.setString("id", myId);
        statement.execute();
    }
}