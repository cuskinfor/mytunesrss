package de.codewave.utils.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * de.codewave.utils.sql.DataStoreSession
 */
public class DataStoreSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataStoreSession.class);

    private DataStore myDataStore;
    private AtomicReference<Connection> myConnection = new AtomicReference<Connection>();
    private boolean myRollbackOnly;

    protected DataStoreSession(DataStore dataStore) {
        myDataStore = dataStore;
    }

    protected synchronized void begin() {
        if (myConnection.get() == null) {
            myConnection.set(myDataStore.acquireConnection());
            try {
                myConnection.get().setReadOnly(false);
            } catch (SQLException e) {
                LOGGER.error("Could not set read-only to false.", e);
            }
            try {
                myConnection.get().setAutoCommit(false);
            } catch (SQLException e) {
                LOGGER.error("Could not set auto-commit to false.", e);
            }
        } else {
            throw new IllegalStateException("Cannot begin a new transaction while another one is pending. First commit or rollback the pending transaction.");
        }
    }

    protected synchronized void release() {
        Connection connection = myConnection.getAndSet(null);
        if (connection != null) {
            myDataStore.releaseConnection(connection);
        }
    }

    public synchronized void commit() {
        Connection connection = myConnection.getAndSet(null);
        if (connection != null) {
            if (myRollbackOnly) {
                LOGGER.debug("Transaction marked for rollback only. Rolling back instead of committing.");
                rollback(connection);
            } else {
                commit(connection);
            }
        }
    }

    private void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Could not commit transaction.", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.error("Could not set auto-commit to true.", e);
            } finally {
                myDataStore.releaseConnection(connection);
            }
        }
    }

    public synchronized void rollback() {
        Connection connection = myConnection.getAndSet(null);
        if (connection != null) {
            rollback(connection);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            LOGGER.error("Could not rollback transaction.", e);
        } finally {
            myDataStore.destroyConnection(connection);
        }
    }

    public synchronized void setRollbackOnly() {
        if (myConnection.get() != null) {
            myRollbackOnly = true;
        }
    }

    public synchronized <T> T executeQuery(DataStoreQuery<T> query) throws SQLException {
        if (myConnection.get() == null) {
            begin();
        }
        LOGGER.trace(myConnection.get().toString() + " -> " + query.getClass().getName());
        return query.execute(myConnection.get());
    }

    public synchronized void executeStatement(DataStoreStatement statement) throws SQLException {
        if (myConnection.get() == null) {
            begin();
        }
        LOGGER.trace(myConnection.get().toString() + " -> " + statement.getClass().getName());
        statement.execute(myConnection.get());
    }

}
