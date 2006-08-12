/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.*;
import org.apache.commons.logging.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;

import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.DataStore
 */
public class DataStore {
    private static final Log LOG = LogFactory.getLog(DataStore.class);
    public static final String DIRNAME = "hsqldb";

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load database driver.", e);
            }
        }
    }

    private GenericObjectPool myConnectionPool;

    public void init() throws IOException {
        String filename = DIRNAME + "/MyTunesRSS";
        String pathname = ProgramUtils.getApplicationDataPath("MyTunesRSS");
        final String connectString = "jdbc:hsqldb:file:" + pathname + "/" + filename;
        myConnectionPool = new GenericObjectPool(new BasePoolableObjectFactory() {
            public Object makeObject() throws Exception {
                return DriverManager.getConnection(connectString, "sa", "");
            }
        }, 10, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 5000, 3, 5, false, false, 10000, 2, 20000, false, 20000);
    }

    public void destroy() {
        Connection connection = null;
        try {
            connection = aquireConnection();
            connection.createStatement().execute("SHUTDOWN COMPACT");
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not shutdown database correctly.", e);
            }
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
            try {
                myConnectionPool.close();
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not close connection pool.", e);
                }
            }
        }
    }

    Connection aquireConnection() {
        try {
            return (Connection)myConnectionPool.borrowObject();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not get connection from pool.", e);
            }
        }
        return null;
    }

    void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                myConnectionPool.returnObject(connection);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not return connection to pool.", e);
                }
            }
        }
    }

    void destroyConnection(Connection connection) {
        try {
            myConnectionPool.invalidateObject(connection);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not return connection to pool.", e);
            }
        }
    }

    public DataStoreSession getTransaction() {
        return new DataStoreSession(this);
    }

    public <T> T executeQuery(DataStoreQuery<T> query) throws SQLException {
        Connection connection = aquireConnection();
        try {
            return query.execute(connection);
        } finally {
            releaseConnection(connection);
        }
    }
}