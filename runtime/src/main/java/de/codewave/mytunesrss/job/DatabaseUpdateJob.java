package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.*;
import org.apache.commons.logging.*;
import org.quartz.*;

/**
 * Job implementation for database updates. This job simply starts the database builder task which
 * only runs if it is not already running. The task is started only if a database update
 * is necessary according to the task.
 */
public class DatabaseUpdateJob implements Job {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DatabaseUpdateJob.class);

    /**
     * Execute the job.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            DatabaseBuilderTask task = MyTunesRss.createDatabaseBuilderTask();
            if (task.needsUpdate()) {
                task.execute();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not automatically update database.", e);
            }
        }
    }
}