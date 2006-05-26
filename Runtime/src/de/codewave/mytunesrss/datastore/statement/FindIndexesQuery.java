/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumIndexesQuery
 */
public abstract class FindIndexesQuery extends DataStoreQuery {
    private String myTable;

    protected FindIndexesQuery(String table) {
        myTable = table;
    }

    public Collection execute(Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT DISTINCT(SUBSTR(UCASE(name), 1, 1)) AS index, COUNT(name) AS index_count FROM " + myTable +
                        " GROUP BY index ORDER BY index");
        Collection<FindIndexesQuery.Index> indexes = new ArrayList<FindIndexesQuery.Index>();
        while (resultSet.next()) {
            if (!InsertTrackStatement.UNKNOWN.equals(resultSet.getString("INDEX"))) {
                indexes.add(new FindIndexesQuery.Index(resultSet.getString("INDEX"), resultSet.getInt("INDEX_COUNT")));
            }
        }
        return indexes;
    }

    public static class Index {
        private String myLetter;
        private int myCount;

        public Index(String letter, int count) {
            myLetter = letter;
            myCount = count;
        }

        public String getLetter() {
            return myLetter;
        }

        public int getCount() {
            return myCount;
        }
    }
}