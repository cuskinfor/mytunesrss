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
    public abstract T execute(Connection connection) throws SQLException;

    protected <E> List<E> execute(PreparedStatement statement, ResultBuilder<E> builder, Object... parameters) throws SQLException {
        statement.clearParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        }
        ResultSet resultSet = statement.executeQuery();
        List<E> results = new ArrayList<E>();
        while (resultSet.next()) {
            results.add(builder.create(resultSet));
        }
        return results;

    }
}