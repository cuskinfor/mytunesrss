/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.ConfirmTrackIdsQuery
 */
public class ConfirmTrackIdsQuery extends DataStoreQuery<Collection<String>> {
    private Collection<String> myIds;

    public ConfirmTrackIdsQuery(Collection<String> ids) {
        myIds = ids;
    }

    public Collection<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "confirmTrackIds");
        statement.setItems("ids", myIds);
        ResultSet resultSet = statement.executeQuery();
        Set<String> trackIds = new HashSet<String>();
        while (resultSet.next()) {
            trackIds.add(resultSet.getString("ID"));
        }
        return trackIds;
    }
}