package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.utils.sql.DataStoreSession;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Job implementation for removal of old statistic events.
 */
public class RemoveStatisticEventsJob implements Job {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveStatisticEventsJob.class);

    /**
     * Execute the job.
     *
     * @param jobExecutionContext
     * @throws org.quartz.JobExecutionException
     *
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            tx.executeStatement(new RemoveOldEventsStatement());
            tx.commit();
        } catch (Exception e) {
            LOGGER.error("Could not remove old statistic events.", e);
        } finally {
            tx.rollback();
        }
    }
}