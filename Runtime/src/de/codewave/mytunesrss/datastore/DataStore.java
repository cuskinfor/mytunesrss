/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import org.apache.commons.logging.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;
import org.apache.commons.lang.*;

import java.sql.*;
import java.util.*;
import java.io.*;

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

    public void init() throws IOException {
        String filename = "hsqldb/MyTunesRSS";
        String pathname = getApplicationDataPath("MyTunesRSS");
        final String connectString = "jdbc:hsqldb:file:" + pathname + "/" + filename;
        myConnectionPool = new GenericObjectPool(new BasePoolableObjectFactory() {
            public Object makeObject() throws Exception {
                return DriverManager.getConnection(connectString, "sa", "");
            }
        }, 10, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 5000, 3, 5, false, false, 10000, 2, 20000, false, 20000);
    }

    private String getApplicationDataPath(String applicationName) throws IOException {
        String pathname = System.getProperty("user.home");
        if (StringUtils.isNotEmpty(pathname)) {
            if (!pathname.endsWith("/") && !pathname.endsWith("\\")) {
                pathname += "/";
            }
        } else {
            pathname = "./";
        }
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
            pathname += "Library/Caches/" + applicationName;
        } else if (ProgramUtils.guessOperatingSystem() == OperatingSystem.Windows) {
            String envAppData = System.getenv("appdata");
            if (StringUtils.isNotEmpty(envAppData)) {
                pathname = envAppData;
                if (!pathname.endsWith("/") && !pathname.endsWith("\\")) {
                    pathname += "/";
                }
                pathname += applicationName;
            } else {
                pathname += "." + applicationName;
            }
        } else {
            pathname += "." + applicationName;
        }
        File path = new File(pathname);
        if (!path.exists()) {
            path.mkdirs();
        }
        return pathname;
    }

    public void destroy() throws Exception {
        Connection connection = aquireConnection();
        try {
            if (connection != null) {
                connection.createStatement().execute("SHUTDOWN COMPACT");
            }
        } finally {
            releaseConnection(connection);
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