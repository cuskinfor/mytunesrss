package de.codewave.utils.sql;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * de.codewave.utils.sql.DataStoreQuery
 */
public abstract class DataStoreQuery<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreQuery.class);

    private ResultSetType myResultSetType = ResultSetType.TYPE_SCROLL_INSENSITIVE;
    private int myFetchSize;

    public void setFetchOptions(ResultSetType resultSetType, int fetchSize) {
        if (fetchSize > 0 && resultSetType != ResultSetType.TYPE_FORWARD_ONLY) {
            throw new IllegalArgumentException("Fetch size supported for forward-only result set type only.");
        }
        myResultSetType = resultSetType;
        myFetchSize = fetchSize;
    }

    public abstract T execute(Connection connection) throws SQLException;

    /*protected <E> StandardQueryResult<E> execute(PreparedStatement statement, ResultBuilder<E> builder, Object... parameters) throws SQLException {
        statement.clearParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        }
        statement.setFetchSize(myFetchSize);
        if (statement.getResultSet() != null) {
            try {
                if (DataStore.OPEN_RESULT_SETS.remove(statement.getResultSet())) {
                    LOGGER.debug("Closing result set (remaining open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
                }
                statement.getResultSet().close();
            } catch (SQLException e) {
                LOGGER.warn("Could not close result set.", e);
            }
        }
        ResultSet resultSet = statement.executeQuery();
        DataStore.OPEN_RESULT_SETS.add(resultSet);
        LOGGER.debug("Opening result set (now open = " + DataStore.OPEN_RESULT_SETS.size() + ").");
        return new StandardQueryResult<E>(resultSet, builder);
    }*/

    protected <E> QueryResult<E> execute(SmartStatement statement, ResultBuilder<E> builder) throws SQLException {
        ResultSet resultSet = statement.executeQuery(myResultSetType, myFetchSize);
        return new StandardQueryResult<E>(resultSet, builder);
    }

    public static interface ResultProcessor<E> {
        void process(E result);
    }

    public static final class NoopResultProcessor implements ResultProcessor {
        public void process(Object result) {
            // ignore
        }
    }

}
