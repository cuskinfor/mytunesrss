/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.MyTunesRss;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job implementation for database backups. This job simply runs the backup function.
 */
public class DatabaseBackupJob implements Job {

    /**
     * Execute the job.
     *
     * @param jobExecutionContext
     * @throws org.quartz.JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseBackup();
    }

}