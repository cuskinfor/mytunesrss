/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackIdsQuery
 */
public class FindTrackIdsQuery extends DataStoreQuery<Collection<String>> {
    private String mySource;

    public FindTrackIdsQuery(String source) {
        mySource = source;
    }

    public Collection<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findTrackIdsForSource");
        statement.setString("source", mySource);
        ResultSet resultSet = statement.executeQuery();
        Set<String> trackIds = new HashSet<String>();
        while (resultSet.next()) {
            trackIds.add(resultSet.getString("ID"));
        }
        return trackIds;
    }
}