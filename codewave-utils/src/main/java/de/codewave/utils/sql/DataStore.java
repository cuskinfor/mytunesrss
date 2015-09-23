package de.codewave.utils.sql;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * de.codewave.utils.sql.DataStore
 */
public abstract class DataStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataStore.class);

    private GenericObjectPool myConnectionPool;
    public static Set<ResultSet> OPEN_RESULT_SETS = Collections.synchronizedSet(new HashSet<ResultSet>());
    private static final Map<Connection, Set<PreparedStatement>> PREPARED_STATEMENT_MEMORY = new HashMap<Connection, Set<PreparedStatement>>();

    protected void setConnectionPool(GenericObjectPool connectionPool) {
        myConnectionPool = connectionPool;
    }

    public abstract void init() throws Exception;

    public void destroy() {
        Connection connection = null;
        try {
            connection = acquireConnection();
            if (connection != null) {
                connection.setReadOnly(false);
                connection.setAutoCommit(true);
                beforeDestroy(connection);
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error in hook before destroying database.", e);
            }
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
            try {
                myConnectionPool.close();
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not close connection pool.", e);
                }
            }
        }
    }

    protected abstract void beforeDestroy(Connection connection) throws SQLException;

    Connection acquireConnection() {
        try {
            return (Connection)myConnectionPool.borrowObject();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get connection from pool.", e);
            }
        }
        return null;
    }

    void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                try {
                    closeResultSetsAndStatements(connection);
                    myConnectionPool.returnObject(connection);
                } catch (SQLException e) {
                    LOGGER.error("Problem closing result sets and statement. Invalidating object pool instance.", e);
                    myConnectionPool.invalidateObject(connection);
                }
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not return connection to pool.", e);
                }
            }
        }
    }

    private void closeResultSetsAndStatements(Connection connection) throws SQLException {
        Set<PreparedStatement> statements = PREPARED_STATEMENT_MEMORY.remove(connection);
        if (statements != null) {
            for (PreparedStatement statement : statements) {
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
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.warn("Could not close statement.", e);
                }
            }
        }
    }

    void destroyConnection(Connection connection) {
        try {
            try {
                closeResultSetsAndStatements(connection);
            } catch (SQLException e) {
                LOGGER.error("Problem closing result sets and statement. Proceeding with pool object instance invalidation.", e);
            }
            myConnectionPool.invalidateObject(connection);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not invalidate connection to pool.", e);
            }
        }
    }

    public DataStoreSession getTransaction() {
        return new DataStoreSession(this);
    }

    public static void addPreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        synchronized (connection) {
            Set<PreparedStatement> statements = PREPARED_STATEMENT_MEMORY.get(connection);
            if (statements == null) {
                statements = new HashSet<PreparedStatement>();
                PREPARED_STATEMENT_MEMORY.put(connection, statements);
            }
            statements.add(preparedStatement);
        }
    }
}
