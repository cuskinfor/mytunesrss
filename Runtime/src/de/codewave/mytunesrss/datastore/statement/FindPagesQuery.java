/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPagesQuery
 */
public class FindPagesQuery extends DataStoreQuery {
    private InsertPageStatement.PagerType myType;

    public FindPagesQuery(InsertPageStatement.PagerType type) {
        myType = type;
    }

    public Collection execute(Connection connection) throws SQLException {
        PreparedStatement query = connection.prepareStatement(
                "SELECT index AS index, value AS value, content_count AS content_count FROM pager WHERE type = ? ORDER by index");
        query.setString(1, myType.name());
        ResultSet resultSet = query.executeQuery();
        List<Pager.Page> pages = new ArrayList<Pager.Page>();
        while (resultSet.next()) {
            pages.add(new Pager.Page<Integer>(Integer.toString(resultSet.getInt("INDEX")), resultSet.getString("VALUE"), new Integer(resultSet.getInt(
                    "CONTENT_COUNT"))));
        }
        return pages.isEmpty() ? null : pages;
    }
}