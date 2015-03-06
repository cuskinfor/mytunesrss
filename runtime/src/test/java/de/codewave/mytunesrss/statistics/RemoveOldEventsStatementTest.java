package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTestUtils;
import de.codewave.utils.sql.DataStoreSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * de.codewave.mytunesrss.statistics.RemoveOldEventsStatementTest
 */
public class RemoveOldEventsStatementTest {
    @Before
    public void setUp() throws SQLException, IOException, ClassNotFoundException {
        MyTunesRssTestUtils.before();
    }

    @After
    public void after() {
        MyTunesRssTestUtils.after();
    }

    @Test
    public void testExecuteStatement() throws SQLException, IOException {
        createEvents();
        // count all events
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            assertEquals(20, session.executeQuery(new GetStatisticsEventsQuery(0, System.currentTimeMillis())).getResultSize());
            session.commit();
            // keep 11 events (keep time = 10 days)
            session = MyTunesRss.STORE.getTransaction();
            MyTunesRss.CONFIG.setStatisticKeepTime(10);
            session.executeStatement(new RemoveOldEventsStatement());
            session.commit();
            // count remaining events
            session = MyTunesRss.STORE.getTransaction();
            assertEquals(11, session.executeQuery(new GetStatisticsEventsQuery(0, System.currentTimeMillis())).getResultSize());
            session.commit();
        } finally {
            session.rollback();
        }
    }

    private void createEvents() throws SQLException {
        // insert 20 events (1 for today, 1 for yesterday, ...)
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            for (long i = 0; i < 20; i++) {
                DownloadEvent event = new DownloadEvent("dummy", "dummy-id", 1000);
                event.setEventTime(System.currentTimeMillis() - (i * 3600L * 1000L * 24L));
                session.executeStatement(new InsertStatisticsEventStatement(event));
            }
            session.executeStatement(new RemoveOldEventsStatement());
            session.commit();
        } finally {
            session.rollback();
        }
    }
}