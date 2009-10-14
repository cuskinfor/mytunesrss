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

import org.apache.commons.lang.StringEscapeUtils;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAllTagsQuery
 */
public class FindAllTagsQuery extends DataStoreQuery<DataStoreQuery.QueryResult<String>> {

    private String myStartsWith;

    public FindAllTagsQuery() {
        myStartsWith = null;
    }

    public FindAllTagsQuery(String startsWith) {
        myStartsWith = startsWith;
    }

    public DataStoreQuery.QueryResult<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTags");
        statement.setString("query", myStartsWith + "%");
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }
}