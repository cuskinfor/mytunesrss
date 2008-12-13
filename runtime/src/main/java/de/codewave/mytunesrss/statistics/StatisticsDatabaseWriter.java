package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter
 */
public class StatisticsDatabaseWriter implements StatisticsEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsDatabaseWriter.class);

    public void handleEvent(final StatisticsEvent event) {
        new Thread(new Runnable() {
            public void run() {
                DataStoreSession tx = MyTunesRss.STORE.getTransaction();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(event);
                    InsertStatisticsEventStatement statisticsEventStatement = new InsertStatisticsEventStatement(baos.toByteArray());
                    tx.executeStatement(statisticsEventStatement);
                    tx.commit();
                    LOGGER.debug("Wrote statistics event \"" + event + "\" to database.");
                } catch (IOException e) {
                    LOGGER.error("Could not write statistics event to database.", e);
                    try {
                        tx.rollback();
                    } catch (SQLException e1) {
                        throw new RuntimeException("Could not rollback transaction.", e);
                    }
                } catch (SQLException e) {
                    try {
                        tx.rollback();
                    } catch (SQLException e1) {
                        throw new RuntimeException("Could not rollback transaction.", e);
                    }
                }
            }
        }, "StatisticsEventWriter").start();
    }
}