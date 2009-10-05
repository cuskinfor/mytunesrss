/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAllTagsQuery
 */
public class FindAllTagsQuery extends DataStoreQuery<DataStoreQuery.QueryResult<String>> {

    public DataStoreQuery.QueryResult<String> execute(Connection connection) throws SQLException {
        return execute(MyTunesRssUtils.createStatement(connection, "findAllTags"), new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }
}