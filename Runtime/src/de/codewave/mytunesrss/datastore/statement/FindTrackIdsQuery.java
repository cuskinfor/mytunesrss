/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackIdsQuery
 */
public class FindTrackIdsQuery extends DataStoreQuery<String> {
    public Collection<String> execute(Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT id AS id FROM track");
        Set<String> trackIds = new HashSet<String>();
        while (resultSet.next()) {
            trackIds.add(resultSet.getString("ID"));
        }
        return trackIds;
    }
}