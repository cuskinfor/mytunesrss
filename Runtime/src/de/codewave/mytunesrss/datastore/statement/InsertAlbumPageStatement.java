/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertPagerPageStatement
 */
public class InsertAlbumPageStatement implements DataStoreStatement {
    private int myIndex;
    private String myKey;
    private String myValue;

    public InsertAlbumPageStatement(int index, String key, String value) {
        myIndex = index;
        myKey = key;
        myValue = value;
    }

    public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO album_pager VALUES ( ?, ?, ? )");
        statement.clearParameters();
        statement.setInt(1, myIndex);
        statement.setString(2, myKey);
        statement.setString(3, myValue);
        statement.execute();
    }
}