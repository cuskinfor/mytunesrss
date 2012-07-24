package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.DatabaseJobRunningException;
import de.codewave.mytunesrss.MyTunesRss;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job implementation for database updates. This job simply starts the database builder task which only runs if it is not already running. The task is
 * started only if a database update is necessary according to the task.
 */
public class DatabaseUpdateJob implements Job {

    /**
     * Execute the job.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.getDatasources(), false);
        } catch (DatabaseJobRunningException e) {
            MyTunesRss.ADMIN_NOTIFY.notifySkippedDatabaseUpdate(jobExecutionContext);
        }
    }

}