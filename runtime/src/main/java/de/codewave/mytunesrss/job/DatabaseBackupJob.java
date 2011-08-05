/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.task.BackupDatabaseCallable;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job implementation for database backups. This job simply runs the backup function.
 */
public class DatabaseBackupJob implements Job {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseBackupJob.class);

    /**
     * Execute the job.
     *
     * @param jobExecutionContext
     * @throws org.quartz.JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            new BackupDatabaseCallable().call();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not automatically backup database.", e);
            }
        }
    }
}