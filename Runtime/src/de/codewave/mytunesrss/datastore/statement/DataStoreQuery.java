/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;
import java.util.*;

/**
 de.codewave.mytunesrss.datastore.statement.DataStoreQueryry
 */
public abstract class DataStoreQuery<T> {
    public abstract Collection<T> execute(Connection connection) throws SQLException;

    protected Collection<T> execute(PreparedStatement statement, ResultBuilder<T> builder, Object... parameters) throws SQLException {
        statement.clearParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        }
        ResultSet resultSet = statement.executeQuery();
        List<T> results = new ArrayList<T>();
        while (resultSet.next()) {
            results.add(builder.create(resultSet));
        }
        return results;

    }
}