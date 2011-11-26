/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement;
import de.codewave.mytunesrss.datastore.statement.InsertPhotoStatement;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.GetStatisticEventsQuery;
import de.codewave.mytunesrss.statistics.InsertStatisticsEventStatement;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.LocaleServiceProviderPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * de.codewave.mytunesrss.statistics.RemoveOldEventsStatementTest
 */
public class MyTunesRssDataStoreTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssDataStoreTest.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Before
    public void setUp() throws SQLException, IOException, ClassNotFoundException {
        MyTunesRss.VERSION = "1.0.0";
        MyTunesRss.CONFIG = new MyTunesRssConfig();
        MyTunesRss.CONFIG.setDatabaseType(DatabaseType.h2);
        MyTunesRss.CONFIG.setDatabaseConnection("jdbc:h2:file:/tmp/" + UUID.randomUUID().toString() + ";DB_CLOSE_DELAY=-1");
        MyTunesRss.CONFIG.setDatabaseUser("sa");
        MyTunesRss.CONFIG.setDatabasePassword("");
        Class.forName("org.h2.Driver");
        MyTunesRss.STORE = new MyTunesRssDataStore();
        MyTunesRss.STORE.init();
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            session.executeStatement(new CreateAllTablesStatement());
            session.commit();
        } finally {
            session.rollback();
        }
    }

    @After
    public void after() {
        MyTunesRssTestUtils.after();
    }

    @Test
    @Ignore // should only be run manually
    public void testDestoryReInitWhiteInUse() throws SQLException, IOException, InterruptedException {
        executorService.submit(new DestroyInitRunnable());
        executorService.submit(new InsertRunnable());
        executorService.submit(new InsertRunnable());
        executorService.submit(new InsertRunnable());
        executorService.submit(new InsertRunnable());
        executorService.submit(new InsertRunnable());
        Thread.sleep(1000 * 60 * 60); // run one hour
        executorService.shutdown();
        executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
   }

    private static class InsertRunnable implements Runnable {
        public void run() {
            while (true) {
                if (Thread.interrupted()) {
                    break;
                }
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                try {
                    InsertPhotoStatement insertPhotoStatement = new InsertPhotoStatement();
                    insertPhotoStatement.setId(UUID.randomUUID().toString());
                    insertPhotoStatement.setName(UUID.randomUUID().toString());
                    insertPhotoStatement.setFile(UUID.randomUUID().toString());
                    session.executeStatement(insertPhotoStatement);
                    session.commit();
                } catch (SQLException e) {
                    LOGGER.error("Bang!", e);
                    session.rollback();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class DestroyInitRunnable implements Runnable {
        public void run() {
            while (true) {
                if (Thread.interrupted()) {
                    break;
                }
                try {
                    Thread.sleep(500);
                    MyTunesRss.STORE.destroy();
                    Thread.sleep(200);
                    MyTunesRss.STORE.init();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    LOGGER.error("Bang!", e);
                    Thread.currentThread().interrupt();
                } catch (SQLException e) {
                    LOGGER.error("Bang!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}