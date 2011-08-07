/*
 * MyTunesRssExecutorService.java 19.04.2010
 * 
 * Copyright (c) 2010 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package de.codewave.mytunesrss;

import de.codewave.mytunesrss.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.*;

public class MyTunesRssExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssExecutorService.class);

    private final ExecutorService DATABASE_JOB_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ExecutorService LUCENE_UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ExecutorService ROUTER_CONFIG_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ScheduledExecutorService GENERAL_EXECUTOR = Executors.newScheduledThreadPool(50);

    private Future<Boolean> DATABASE_UPDATE_FUTURE;

    private Future<Void> DATABASE_RESET_FUTURE;

    private Future<Void> DATABASE_BACKUP_FUTURE;

    private ScheduledFuture MYTUNESRSSCOM_UPDATE_FUTURE;

    public void shutdown() throws InterruptedException {
        DATABASE_JOB_EXECUTOR.shutdownNow();
        DATABASE_JOB_EXECUTOR.awaitTermination(10000, TimeUnit.MILLISECONDS);
        LUCENE_UPDATE_EXECUTOR.shutdownNow();
        LUCENE_UPDATE_EXECUTOR.awaitTermination(10000, TimeUnit.MILLISECONDS);
        GENERAL_EXECUTOR.shutdownNow();
        GENERAL_EXECUTOR.awaitTermination(10000, TimeUnit.MILLISECONDS);
        ROUTER_CONFIG_EXECUTOR.shutdownNow();
        ROUTER_CONFIG_EXECUTOR.awaitTermination(10000, TimeUnit.MILLISECONDS);
    }

    public synchronized void scheduleDatabaseUpdate(boolean ignoreTimestamps) {
        DatabaseBuilderCallable databaseBuilderCallable = new DatabaseBuilderCallable(ignoreTimestamps);
        boolean needsUpdate = true;
        try {
            needsUpdate = databaseBuilderCallable.needsUpdate();
        } catch (SQLException e) {
            LOGGER.warn("Could not determine if database needs an update, forcing update.", e);
        }
        if (needsUpdate) {
            cancelDatabaseUpdateAndResetJob();
            try {
                DATABASE_UPDATE_FUTURE = DATABASE_JOB_EXECUTOR.submit(databaseBuilderCallable);
            } catch (RejectedExecutionException e) {
                LOGGER.error("Could not schedule database update task.", e);
            }
        }
    }

    public void scheduleImageUpdate() {
        cancelDatabaseUpdateAndResetJob();
        try {
            DATABASE_UPDATE_FUTURE = DATABASE_JOB_EXECUTOR.submit(new ForcedImageUpdateCallable(MyTunesRss.CONFIG.isIgnoreTimestamps()));
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule image update task.", e);
        }
    }

    public synchronized void scheduleDatabaseReset() {
        cancelDatabaseUpdateAndResetJob();
        try {
            DATABASE_RESET_FUTURE = DATABASE_JOB_EXECUTOR.submit(new RecreateDatabaseCallable());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule database reset task.", e);
        }
    }

    public synchronized void scheduleDatabaseBackup() {
        cancelDatabaseBackupJob();
        try {
            DATABASE_BACKUP_FUTURE = DATABASE_JOB_EXECUTOR.submit(new BackupDatabaseCallable());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule database backup task.", e);
        }
    }

    public synchronized void cancelDatabaseUpdateAndResetJob() {
        if (DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone()) {
            DATABASE_UPDATE_FUTURE.cancel(true);
        }
        if (DATABASE_RESET_FUTURE != null && !DATABASE_RESET_FUTURE.isDone()) {
            DATABASE_RESET_FUTURE.cancel(true);
        }
    }

    public synchronized void cancelDatabaseBackupJob() {
        if (DATABASE_BACKUP_FUTURE != null && !DATABASE_BACKUP_FUTURE.isDone()) {
            DATABASE_BACKUP_FUTURE.cancel(true);
        }
    }

    public synchronized boolean isDatabaseUpdateRunning() {
        return DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone();
    }

    public synchronized boolean isDatabaseResetRunning() {
        return DATABASE_RESET_FUTURE != null && !DATABASE_RESET_FUTURE.isDone();
    }

    public synchronized boolean isDatabaseBackupRunning() {
        return DATABASE_BACKUP_FUTURE != null && !DATABASE_BACKUP_FUTURE.isDone();
    }

    public synchronized void scheduleLuceneAndSmartPlaylistUpdate(String[] trackIds) {
        try {
            LUCENE_UPDATE_EXECUTOR.submit(new RefreshSmartPlaylistsAndLuceneIndexCallable(trackIds));
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule lucene and smart playlist update task.", e);
        }
    }

    public synchronized void scheduleMyTunesRssComUpdate() {
        try {
            MYTUNESRSSCOM_UPDATE_FUTURE = GENERAL_EXECUTOR.scheduleWithFixedDelay(new MyTunesRssComUpdateRunnable(), 0, 300, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule mytunesrss.com update task.", e);
        }
    }

    public synchronized void executeMyTunesRssComUpdate() {
        try {
            GENERAL_EXECUTOR.execute(new MyTunesRssComUpdateRunnable());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not execute mytunesrss.com update task.", e);
        }
    }

    public synchronized void cancelMyTunesRssComUpdate() {
        if (MYTUNESRSSCOM_UPDATE_FUTURE != null && !MYTUNESRSSCOM_UPDATE_FUTURE.isDone() && !MYTUNESRSSCOM_UPDATE_FUTURE.isCancelled()) {
            MYTUNESRSSCOM_UPDATE_FUTURE.cancel(true);
        }
    }

    public synchronized void scheduleExternalAddressUpdate() {
        try {
            GENERAL_EXECUTOR.scheduleWithFixedDelay(new FetchExternalAddressRunnable(), 0, 60, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule external address update task.", e);
        }
    }

    public synchronized void scheduleUpdateCheck() {
        try {
            GENERAL_EXECUTOR.scheduleWithFixedDelay(new CheckUpdateRunnable(), 0, 3600, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule update check task.", e);
        }
    }

    public synchronized void schedule(Runnable runnable, int delay, TimeUnit timeUnit) {
        try {
            GENERAL_EXECUTOR.schedule(runnable, delay, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
    }

    public synchronized <T> ScheduledFuture<T> schedule(Callable<T> callable, int delay, TimeUnit timeUnit) {
        try {
            return GENERAL_EXECUTOR.schedule(callable, delay, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
        return null;
    }

    public synchronized void scheduleWithFixedDelay(Runnable runnable, int initialDelay, int delay, TimeUnit timeUnit) {
        try {
            GENERAL_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
    }

    public synchronized void submitRouterConfig(Runnable runnable) {
        try {
            ROUTER_CONFIG_EXECUTOR.submit(runnable);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
    }
}
