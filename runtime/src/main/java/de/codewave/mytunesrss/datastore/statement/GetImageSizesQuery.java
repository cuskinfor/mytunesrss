/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
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
 * de.codewave.mytunesrss.datastore.statement.InsertImageStatement
 */
public class GetImageSizesQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Integer>> {
    private String myHash;

    public GetImageSizesQuery(String hash) {
        myHash = hash;
    }

    public QueryResult<Integer> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getImageSizes");
        statement.setString("hash", myHash);
        return execute(statement, new ResultBuilder<Integer>() {
            public Integer create(ResultSet resultSet) throws SQLException {
                return resultSet.getInt(1);
            }
        });
    }
}