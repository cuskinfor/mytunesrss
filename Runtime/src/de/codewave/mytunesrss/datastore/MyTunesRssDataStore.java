/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.utils.*;
import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;

import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.MyTunesRssDataStore
 */
public class MyTunesRssDataStore extends DataStore {
    private static final Log LOG = LogFactory.getLog(MyTunesRssDataStore.class);
    public static final String DIRNAME = "h2";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load database driver.", e);
            }
        }
    }

    public void init() throws IOException {
        String filename = DIRNAME + "/MyTunesRSS";
        String pathname = PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        final String connectString = "jdbc:h2:file:" + pathname + "/" + filename;
        setConnectionPool(new GenericObjectPool(new BasePoolableObjectFactory() {
            public Object makeObject() throws Exception {
                long endTime = System.currentTimeMillis() + 10000;
                do {
                    try {
                        return DriverManager.getConnection(connectString, "sa", "");
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
                    return DriverManager.getConnection(connectString, "sa", "");
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not get a database connection.", e);
                    }
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.noDatabaseConnection"));
                System.exit(0);
                return null;
            }
        }, 10, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 5000, 3, 5, false, false, 10000, 2, 20000, false, 20000));
    }
}
