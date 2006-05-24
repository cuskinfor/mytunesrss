/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPagesStatement
 */
public class FindPageConditionStatement extends DataStoreQuery {
    private int myIndex;

    public FindPageConditionStatement(int index) {
        myIndex = index;
    }

    public Collection execute(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT condition AS condition FROM pager WHERE index = ?");
        statement.setInt(1, myIndex);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return Collections.singletonList(resultSet.getString("CONDITION"));
        }
        return null;
    }
}