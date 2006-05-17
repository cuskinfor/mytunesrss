/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.DataStore
 */
public class DataStore {
    private static final Log LOG = LogFactory.getLog(DataStore.class);

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

    public void init() {
        myConnectionPool = new GenericObjectPool(new BasePoolableObjectFactory() {
            public Object makeObject() throws Exception {
                return DriverManager.getConnection("jdbc:hsqldb:file:hsqldb/MyTunesRSS-" + MyTunesRss.VERSION, "sa", "");
            }
        }, 10, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 5000, 3, 5, false, false, 10000, 2, 20000, false, 20000);
    }

    public void destroy() throws Exception {
        try {
            aquireConnection().createStatement().execute("SHUTDOWN COMPACT");
        } finally {
            myConnectionPool.close();
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
        try {
            myConnectionPool.returnObject(connection);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not return connection to pool.", e);
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

    public <T> Collection<T> executeQuery(DataStoreQuery<T> query) throws SQLException {
        Connection connection = aquireConnection();
        try {
            return query.execute(connection);
        } finally {
            releaseConnection(connection);
        }
    }
}