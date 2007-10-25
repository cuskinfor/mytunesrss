/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.xml.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;

import java.io.*;
import java.net.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.MyTunesRssDataStore
 */
public class MyTunesRssDataStore extends DataStore {
    private static final Log LOG = LogFactory.getLog(MyTunesRssDataStore.class);
    public static final String DIRNAME = "h2";
    public static final int UPDATE_HELP_TABLES_FREQUENCY = 30000;

    private SmartStatementFactory mySmartStatementFactory;

    public void init() throws IOException {
        initSmartStatementFactory();
        String filename = DIRNAME + "/MyTunesRSS";
        String pathname = PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        final String connectString = System.getProperty("database.connection", "jdbc:h2:file:" + pathname + "/" + filename);
        setConnectionPool(new GenericObjectPool(new BasePoolableObjectFactory() {
            public Object makeObject() throws Exception {
                long endTime = System.currentTimeMillis() + 10000;
                do {
                    try {
                        return DriverManager.getConnection(connectString, System.getProperty("database.user", "sa"), System.getProperty(
                                "database.password",
                                ""));
                    } catch (SQLException e1) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Could not get a database connection.");
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // intentionally left blank
                    }
                } while (System.currentTimeMillis() < endTime);
                try {
                    return DriverManager.getConnection(connectString, System.getProperty("database.user", "sa"), System.getProperty(
                            "database.password",
                            ""));
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not get a database connection.", e);
                    }
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.noDatabaseConnection"));
                MyTunesRssUtils.shutdown();
                return null;
            }

            @Override
            public void destroyObject(Object object) throws Exception {
                if (object instanceof Connection) {
                    ((Connection)object).close();
                }
            }
        }, 50, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, -1, 20, 0, false, false, 5000, 5, 10000, false, 10000));
    }

    @Override
    protected void beforeDestroy(Connection connection) throws SQLException {
        if (System.getProperty("database.connection") == null) {
            connection.createStatement().execute("SHUTDOWN COMPACT");
        }
    }

    private void initSmartStatementFactory() {
        String databaseType = System.getProperty("database.type", "h2");
        JXPathContext[] contexts =
                new JXPathContext[] {JXPathUtils.getContext(getClass().getResource("ddl.xml")), JXPathUtils.getContext(getClass().getResource(
                        "dml.xml")), JXPathUtils.getContext(getClass().getResource("migration.xml"))};
        URL url = getClass().getResource("ddl_" + databaseType + ".xml");
        if (url != null) {
            contexts = (JXPathContext[])ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        url = getClass().getResource("dml_" + databaseType + ".xml");
        if (url != null) {
            contexts = (JXPathContext[])ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        url = getClass().getResource("migration_" + databaseType + ".xml");
        if (url != null) {
            contexts = (JXPathContext[])ArrayUtils.add(contexts, JXPathUtils.getContext(url));
        }
        mySmartStatementFactory = SmartStatementFactory.getInstance(contexts);
    }

    public SmartStatementFactory getSmartStatementFactory() {
        return mySmartStatementFactory;
    }
}
