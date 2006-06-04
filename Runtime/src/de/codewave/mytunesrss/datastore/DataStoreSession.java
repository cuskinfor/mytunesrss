/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.DataStoreSession
 */
public class DataStoreSession {
    private static final Log LOG = LogFactory.getLog(DataStoreSession.class);

    private DataStore myDataStore;
    private Connection myConnection;
    private boolean myRollbackOnly;

    DataStoreSession(DataStore dataStore) {
        myDataStore = dataStore;
    }

    public synchronized void begin() {
        if (myConnection == null) {
            myConnection = myDataStore.aquireConnection();
            try {
                myConnection.setAutoCommit(false);
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not set auto-commit to false.", e);
                }
            }
        } else {
            throw new IllegalStateException(
                    "Cannot begin a new transaction while another one is pending. First commit or rollback the pending transaction.");
        }
    }

    public synchronized void commitAndContinue() throws SQLException {
        if (myRollbackOnly) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction marked for rollback only. Rolling back instead of committing.");
            }
            rollbackAndContinue();
        } else if (myConnection == null) {
            throw new IllegalStateException("No pending transaction to commit.");
        } else {
            try {
                myConnection.commit();
            } catch (SQLException e) {
                myDataStore.destroyConnection(myConnection);
                myConnection = null;
                throw e;
            }
        }
    }

    public synchronized void commit() throws SQLException {
        if (myRollbackOnly) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transaction marked for rollback only. Rolling back instead of committing.");
            }
            rollback();
            myConnection.setAutoCommit(true);
            myDataStore.releaseConnection(myConnection);
        } else if (myConnection == null) {
            throw new IllegalStateException("No pending transaction to commit.");
        } else {
            try {
                myConnection.commit();
                myConnection.setAutoCommit(true);
                myDataStore.releaseConnection(myConnection);
            } catch (SQLException e) {
                myDataStore.destroyConnection(myConnection);
                throw e;
            } finally {
                myConnection = null;
            }
        }
    }

    public synchronized void setRollbackOnly() {
        myRollbackOnly = true;
    }

    public synchronized void rollback() throws SQLException {
        if (myConnection == null) {
            throw new IllegalStateException("No pending transaction to rollback.");
        } else {
            try {
                myConnection.rollback();
                myConnection.setAutoCommit(true);
                myDataStore.releaseConnection(myConnection);
            } catch (SQLException e) {
                myDataStore.destroyConnection(myConnection);
                throw e;
            } finally {
                myConnection = null;
            }
        }
    }

    public synchronized void rollbackAndContinue() throws SQLException {
        if (myConnection == null) {
            throw new IllegalStateException("No pending transaction to rollback.");
        } else {
            try {
                myConnection.rollback();
            } catch (SQLException e) {
                myDataStore.destroyConnection(myConnection);
                myConnection = null;
                throw e;
            }
        }
    }

    public synchronized <T> T executeQuery(DataStoreQuery<T> query) throws SQLException {
        if (myConnection == null) {
            Connection connection = myDataStore.aquireConnection();
            try {
                return query.execute(connection);
            } finally {
                myDataStore.releaseConnection(connection);
            }
        } else {
            return query.execute(myConnection);
        }
    }

    public synchronized <T> T getFirstQueryResult(DataStoreQuery<Collection<T>> query) throws SQLException {
        Collection<T> results;
        if (myConnection == null) {
            Connection connection = myDataStore.aquireConnection();
            try {
                results = query.execute(connection);
            } finally {
                myDataStore.releaseConnection(connection);
            }
        } else {
            results = query.execute(myConnection);
        }
        return results.isEmpty() ? null : results.iterator().next();
    }

    public synchronized void executeStatement(DataStoreStatement statement) throws SQLException {
        if (myConnection == null) {
            throw new IllegalStateException("No pending transaction for executing a query.");
        } else {
            statement.execute(myConnection);
        }
    }

    public synchronized PreparedStatement prepare(String sql) throws SQLException {
        return myConnection.prepareStatement(sql);
    }
}