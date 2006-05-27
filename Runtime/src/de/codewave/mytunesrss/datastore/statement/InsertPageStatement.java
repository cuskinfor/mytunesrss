/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertPagerPageStatement
 */
public class InsertPageStatement implements DataStoreStatement {
    public static enum PagerType {
        Album(), Artist();
    }

    private PagerType myType;
    private int myIndex;
    private String myCondition;
    private String myValue;
    private int myContentCount;

    public InsertPageStatement(PagerType type, int index, String condition, String value, int contentCount) {
        myType = type;
        myIndex = index;
        myCondition = condition;
        myValue = value;
        myContentCount = contentCount;
    }

    public void execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO pager VALUES ( ?, ?, ?, ?, ? )");
        statement.clearParameters();
        statement.setString(1, myType.name());
        statement.setInt(2, myIndex);
        statement.setString(3, myCondition);
        statement.setString(4, myValue);
        statement.setInt(4, myContentCount);
        statement.execute();
    }
}