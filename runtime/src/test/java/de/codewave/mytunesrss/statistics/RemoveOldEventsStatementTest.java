package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTestUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * de.codewave.mytunesrss.statistics.RemoveOldEventsStatementTest
 */
public class RemoveOldEventsStatementTest {
    @Before
    public void setUp() throws SQLException, IOException, ClassNotFoundException, NoSuchAlgorithmException {
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
        assertEquals(21, session.executeQuery(new GetStatisticEventsQuery(0, System.currentTimeMillis(), "yyyy-MM-dd")).size());
        session.commit();
        // keep 11 events (keep time = 10 days)
        session = MyTunesRss.STORE.getTransaction();
        MyTunesRss.CONFIG.setStatisticKeepTime(10);
        session.executeStatement(new RemoveOldEventsStatement());
        session.commit();
        // count remaining events
        session = MyTunesRss.STORE.getTransaction();
        assertEquals(12, session.executeQuery(new GetStatisticEventsQuery(0, System.currentTimeMillis(), "yyyy-MM-dd")).size());
        session.commit();
        // count remaining events with other date pattern (should only return one event)
        session = MyTunesRss.STORE.getTransaction();
        List<String> lines = session.executeQuery(new GetStatisticEventsQuery(0, System.currentTimeMillis(), "-"));
        assertEquals(2, lines.size());
        // check returned line with accumulated events (11 download events with 1000 bytes each, all for user "dummy")
        assertEquals("-,dummy,0,11000,0", lines.get(1));
        session.commit();
    }

    private void createEvents() throws IOException, SQLException {
        // insert 20 events (1 for today, 1 for yesterday, ...)
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        for (long i = 0; i < 20; i++) {
            final long finalTime = System.currentTimeMillis() - (i * 3600L * 1000L * 24L);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(new DownloadEvent(new User("dummy"), 1000));
            session.executeStatement(new InsertStatisticsEventStatement(baos.toByteArray()) {
                @Override
                long getTime() {
                    return finalTime;
                }
            });
        }
        session.executeStatement(new RemoveOldEventsStatement());
        session.commit();
    }
}