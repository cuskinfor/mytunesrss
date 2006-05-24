/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertPagerPageStatement
 */
public class InsertPageStatement implements DataStoreStatement {
    private int myIndex;
    private String myCondition;
    private String myValue;

    public InsertPageStatement(int index, String condition, String value) {
        myIndex = index;
        myCondition = condition;
        myValue = value;
    }

    public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO pager VALUES ( ?, ?, ? )");
        statement.clearParameters();
        statement.setInt(1, myIndex);
        statement.setString(2, myCondition);
        statement.setString(3, myValue);
        statement.execute();
    }
}