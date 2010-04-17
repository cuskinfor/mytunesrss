/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("query", StringUtils.isNotBlank(myStartsWith));
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTags", conditionals);
        statement.setString("query", myStartsWith + "%");
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }
}