/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.DeletePlaylistStatement
 */
public class DeletePlaylistStatement implements DataStoreStatement {
    private String myId;

    public void setId(String id) {
        myId = id;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "deletePlaylistById");
        statement.setString("id", myId);
        statement.execute();
    }
}