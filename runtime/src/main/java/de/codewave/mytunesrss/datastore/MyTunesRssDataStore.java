/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.config.DatabaseType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.*;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.datastore.MyTunesRssDataStore
 */
public class MyTunesRssDataStore extends DataStore {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssDataStore.class);

    private SmartStatementFactory mySmartStatementFactory;
    private AtomicBoolean myInitialized = new AtomicBoolean(false);
    GenericObjectPool myConnectionPool;

    public boolean isInitialized() {
        return myInitialized.get();
    }

    @Override
    public synchronized void init() throws IOException, SQLException {
        String databaseConnection = MyTunesRss.CONFIG.getDatabaseConnection();
        if (StringUtils.isNotBlank(MyTunesRss.CONFIG.getDatabaseConnectionOptions())) {
            if (databaseConnection.contains("?")) {
                databaseConnection += "&" + StringUtils.trim(MyTunesRss.CONFIG.getDatabaseConnectionOptions());
            } else {
                databaseConnection += "?" + StringUtils.trim(MyTunesRss.CONFIG.getDatabaseConnectionOptions());
            }
        }
        String databaseUser = MyTunesRss.CONFIG.getDatabaseUser();
        String databasePassword = MyTunesRss.CONFIG.getDatabasePassword();
        initSmartStatementFactory(MyTunesRss.CONFIG.getDatabaseType());
        LOG.info("Creating database connection pool for connect string \"" + databaseConnection + "\".");
        myConnectionPool = new MyTunesRssDataStorePool(databaseConnection, databaseUser, databasePassword);
        setConnectionPool(myConnectionPool);
        testDatabaseConnection(databaseConnection, databaseUser, databasePassword);
        myInitialized.set(true);
        LOG.info("Pool has been marked initialized.");
    }

    private void testDatabaseConnection(String databaseConnection, String databaseUser, String databasePassword) throws SQLException {
        Connection conn = DriverManager.getConnection(databaseConnection, databaseUser, databasePassword);
        conn.close();
    }

    @Override
    protected void beforeDestroy(Connection connection) throws SQLException {
        if (MyTunesRss.CONFIG.isDefaultDatabase()) {
            connection.createStatement().execute("SHUTDOWN COMPACT");
        }
    }

    private void initSmartStatementFactory(DatabaseType databaseType) {
        LOG.info("Using DML/DDL for database type \"" + databaseType.name() + "\" with dialect \"" + databaseType.getDialect() + "\".");
        JXPathContext[] contexts =
                new JXPathContext[]{JXPathUtils.getContext(getClass().getResource("ddl.xml")), JXPathUtils.getContext(getClass().getResource(
                        "dml.xml")), JXPathUtils.getContext(getClass().getResource("migration.xml"))};
        URL url = getClass().getResource("ddl_" + databaseType.getDialect() + ".xml");
        if (url != null) {
            contexts = (JXPathContext[]) ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        url = getClass().getResource("dml_" + databaseType.getDialect() + ".xml");
        if (url != null) {
            contexts = (JXPathContext[]) ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        url = getClass().getResource("migration_" + databaseType.getDialect() + ".xml");
        if (url != null) {
            contexts = (JXPathContext[]) ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        mySmartStatementFactory = SmartStatementFactory.getInstance(contexts);
    }

    public SmartStatementFactory getSmartStatementFactory() {
        return mySmartStatementFactory;
    }

    @Override
    public synchronized void destroy() {
        myInitialized.set(false);
        LOG.info("Pool has been marked uninitialized.");
        while (myConnectionPool != null && myConnectionPool.getNumActive() > 0) {
            LOG.info("Waiting for pool to become inactive (" + myConnectionPool.getNumActive() + ").");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //Thread.currentThread().interrupt();
            }
        }
        LOG.info("Pool is inactive, proceeding with destruction.");
        super.destroy();
    }

    public <T> T executeQuery(DataStoreQuery<T> query) throws SQLException {
        DataStoreSession transaction = getTransaction();
        try {
            return transaction.executeQuery(query);
        } finally {
            transaction.rollback();
        }
    }

    public void executeStatement(DataStoreStatement statement) throws SQLException{
        DataStoreSession transaction = getTransaction();
        try {
            transaction.executeStatement(statement);
            transaction.commit();
        } catch (SQLException e) {
            transaction.rollback();
            throw e;
        }
    }

    @Override
    public DataStoreSession getTransaction() {
        return new DataStoreSession(this) {
            @Override
            protected void begin() {
                try {
                    for (boolean gotConnection = false; !gotConnection; ) {
                        if (!myInitialized.get()) {
                            LOG.debug("Waiting for pool to become initialized.");
                            while (!myInitialized.get()) {
                                Thread.sleep(100);
                            }
                        }
                        super.begin();
                        if (!myInitialized.get()) {
                            LOG.debug("Releasing acquired connection since data store become uninitialized.");
                            release();
                        } else {
                            gotConnection = true;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    public int getQueryResultSize(DataStoreQuery<? extends DataStoreQuery.QueryResult> query) throws SQLException {
        DataStoreSession transaction = getTransaction();
        try {
            return transaction.executeQuery(query).getResultSize();
        } finally {
            transaction.rollback();
        }
    }
}
