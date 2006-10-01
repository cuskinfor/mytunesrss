/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.utils.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;
import org.apache.commons.pool.*;
import org.apache.commons.pool.impl.*;
import org.hsqldb.*;

import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.MyTunesRssDataStore
 */
public class MyTunesRssDataStore extends DataStore {
    private static final Log LOG = LogFactory.getLog(MyTunesRssDataStore.class);
    public static final String DIRNAME = "hsqldb";

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not loadFromPrefs database driver.", e);
            }
        }
    }

    public void init() throws IOException {
        String filename = DIRNAME + "/MyTunesRSS";
        String pathname = ProgramUtils.getCacheDataPath("MyTunesRSS");
        final String connectString = "jdbc:hsqldb:file:" + pathname + "/" + filename;
        setConnectionPool(new GenericObjectPool(new BasePoolableObjectFactory() {
            public Object makeObject() throws Exception {
                return DriverManager.getConnection(connectString, "sa", "");
            }
        }, 10, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 5000, 3, 5, false, false, 10000, 2, 20000, false, 20000));
    }
}
