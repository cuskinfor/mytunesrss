/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

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
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertPagerPage");
        statement.clearParameters();
        statement.setString("type", myType.name());
        statement.setInt("index", myIndex);
        statement.setString("condition", myCondition);
        statement.setString("value", myValue);
        statement.setInt("count", myContentCount);
        statement.execute();
    }
}