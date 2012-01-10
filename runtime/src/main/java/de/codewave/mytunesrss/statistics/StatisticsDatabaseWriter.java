package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter
 */
public class StatisticsDatabaseWriter implements StatisticsEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsDatabaseWriter.class);

    public void handleEvent(final StatisticsEvent event) {
        if (MyTunesRss.CONFIG.getStatisticKeepTime() > 0) {
            // write events only in case the keep time is greater than 0
            new Thread(new Runnable() {
                public void run() {
                    DataStoreSession tx = MyTunesRss.STORE.getTransaction();
                    try {
                        InsertStatisticsEventStatement statisticsEventStatement = new InsertStatisticsEventStatement(event);
                        tx.executeStatement(statisticsEventStatement);
                        tx.commit();
                        LOGGER.debug("Wrote statistics event \"" + event + "\" to database.");
                    } catch (SQLException e) {
                        LOGGER.error("Could not write statistics event to database.", e);
                    } finally {
                        tx.rollback();                    }
                }
            }, "StatisticsEventWriter").start();
        }
    }
}