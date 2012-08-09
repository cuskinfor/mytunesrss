/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAllTagsQuery
 */
public class FindAllTagsForTrackQuery extends DataStoreQuery<DataStoreQuery.QueryResult<String>> {

    private String myTrackId;

    public FindAllTagsForTrackQuery(String trackId) {
        myTrackId = trackId;
    }

    public QueryResult<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTagsForTrack");
        statement.setString("track_id", myTrackId);
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString("tag");
            }
        });
    }
}