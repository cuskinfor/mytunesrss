/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MyTunesRssDataStorePool extends GenericObjectPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssDataStorePool.class);
    private static final int LEAK_TIME = 300000; // 5 minutes
    private static final int CHECK_TIME = 60000; // 60 seconds

    private Map<Object, BorrowInformation> myActiveObjects = new ConcurrentHashMap<Object, BorrowInformation>();

    private AtomicLong myLastValidationTimestamp = new AtomicLong();

    public MyTunesRssDataStorePool(final String databaseConnection, final String databaseUser, final String databasePassword) {
        super(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return DriverManager.getConnection(databaseConnection, databaseUser, databasePassword);
            }

            @Override
            public void destroyObject(Object object) throws Exception {
                if (object instanceof Connection) {
                    ((Connection) object).close();
                }
            }
        }, 50, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, 30000, 5, 2, false, false, 15000, 10, 300000, false, 60000);
    }

    @Override
    public Object borrowObject() throws Exception {
        check();
        Object o = super.borrowObject();
        myActiveObjects.put(o, new BorrowInformation(new Throwable()));
        return o;
    }

    @Override
    public void returnObject(Object obj) throws Exception {
        check();
        myActiveObjects.remove(obj);
        super.returnObject(obj);
    }

    @Override
    public void invalidateObject(Object obj) throws Exception {
        check();
        myActiveObjects.remove(obj);
        super.invalidateObject(obj);
    }

    private void check() {
        if (System.currentTimeMillis() - myLastValidationTimestamp.get() > CHECK_TIME) {
            myLastValidationTimestamp.set(System.currentTimeMillis());
            for (BorrowInformation borrowInformation : new ArrayList<BorrowInformation>(myActiveObjects.values())) {
                if (borrowInformation.getAgeMillis() > LEAK_TIME) {
                    LOGGER.error("Connection in database connection pool seems to have leaked. Allocation (" + new Date(borrowInformation.getTimestamp()) + ") information follows.", borrowInformation.getThreadInfo());
                }
            }
        }
    }
}
