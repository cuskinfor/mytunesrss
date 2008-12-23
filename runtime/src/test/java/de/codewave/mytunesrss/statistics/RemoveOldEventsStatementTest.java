package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssConfig;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement;
import de.codewave.utils.sql.DataStoreSession;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * de.codewave.mytunesrss.statistics.RemoveOldEventsStatementTest
 */
public class RemoveOldEventsStatementTest {
    @Before
    public void setUp() throws SQLException, IOException, ClassNotFoundException {
        MyTunesRss.HEADLESS = true;
        MyTunesRss.VERSION = "1.0.0";
        MyTunesRss.CONFIG = new MyTunesRssConfig();
        MyTunesRss.CONFIG.setDatabaseType("h2");
        MyTunesRss.CONFIG.setDatabaseConnection("jdbc:h2:mem:mytunesrss;DB_CLOSE_DELAY=-1");
        MyTunesRss.CONFIG.setDatabaseUser("sa");
        MyTunesRss.CONFIG.setDatabasePassword("");
        Class.forName("org.h2.Driver");
        MyTunesRss.STORE = new MyTunesRssDataStore();
        MyTunesRss.STORE.init();
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        session.executeStatement(new CreateAllTablesStatement());
        session.commit();
    }

    @Test
    public void testExecuteStatement() throws SQLException, IOException {
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
        // count all events
        session = MyTunesRss.STORE.getTransaction();
        assertEquals(21, session.executeQuery(new GetStatisticEventsQuery(0, System.currentTimeMillis(), "yyyy-MM-dd")).size());
        session.commit();
        // keep 10 events (keep time = 9 days)
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
}