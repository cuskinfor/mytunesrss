/*
 * MyTunesRssExecutorService.java 19.04.2010
 * 
 * Copyright (c) 2010 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package de.codewave.mytunesrss;

import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.mytunesrss.task.ForcedImageUpdateCallable;
import de.codewave.mytunesrss.task.RecreateDatabaseCallable;
import de.codewave.mytunesrss.task.RefreshSmartPlaylistsAndLuceneIndexCallable;

import java.util.concurrent.*;

public class MyTunesRssExecutorService {

    private final ExecutorService DATABASE_JOB_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ExecutorService LUCENE_UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ScheduledExecutorService GENERAL_EXECUTOR = Executors.newScheduledThreadPool(50);

    private Future<Boolean> DATABASE_UPDATE_FUTURE;

    private Future<Void> DATABASE_RESET_FUTURE;

    private ScheduledFuture MYTUNESRSSCOM_UPDATE_FUTURE;

    public synchronized void scheduleDatabaseUpdate(boolean ignoreTimestamps) {
        cancelDatabaseJob();
        DATABASE_UPDATE_FUTURE = DATABASE_JOB_EXECUTOR.submit(new DatabaseBuilderCallable(ignoreTimestamps));
    }

    public void scheduleImageUpdate() {
        cancelDatabaseJob();
        DATABASE_UPDATE_FUTURE = DATABASE_JOB_EXECUTOR.submit(new ForcedImageUpdateCallable(MyTunesRss.CONFIG.isIgnoreTimestamps()));
    }

    public synchronized void scheduleDatabaseReset() {
        cancelDatabaseJob();
        DATABASE_RESET_FUTURE = DATABASE_JOB_EXECUTOR.submit(new RecreateDatabaseCallable());
    }

    public synchronized void cancelDatabaseJob() {
        if (DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone() && !DATABASE_UPDATE_FUTURE.isCancelled()) {
            DATABASE_UPDATE_FUTURE.cancel(true);
        }
        if (DATABASE_RESET_FUTURE != null && !DATABASE_RESET_FUTURE.isDone() && !DATABASE_RESET_FUTURE.isCancelled()) {
            DATABASE_RESET_FUTURE.cancel(true);
        }
    }

    public synchronized boolean isDatabaseUpdateRunning() {
        return DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone()
                && !DATABASE_UPDATE_FUTURE.isCancelled();
    }

    public synchronized boolean isDatabaseResetRunning() {
        return DATABASE_RESET_FUTURE != null && !DATABASE_RESET_FUTURE.isDone()
                && !DATABASE_RESET_FUTURE.isCancelled();
    }

    public synchronized boolean getDatabaseUpdateResult() throws InterruptedException, ExecutionException {
        return DATABASE_UPDATE_FUTURE != null ? DATABASE_UPDATE_FUTURE.get() : false;
    }

    public synchronized void scheduleLuceneAndSmartPlaylistUpdate(String[] trackIds) {
        LUCENE_UPDATE_EXECUTOR.submit(new RefreshSmartPlaylistsAndLuceneIndexCallable(trackIds));
    }

    public synchronized void scheduleMyTunesRssComUpdate() {
        MYTUNESRSSCOM_UPDATE_FUTURE = GENERAL_EXECUTOR.scheduleWithFixedDelay(new MyTunesRssComUpdateRunnable(), 0, 300, TimeUnit.SECONDS);
    }

    public synchronized void executeMyTunesRssComUpdate() {
        GENERAL_EXECUTOR.execute(new MyTunesRssComUpdateRunnable());
    }

    public synchronized void cancelMyTunesRssComUpdate() {
        if (MYTUNESRSSCOM_UPDATE_FUTURE != null && !MYTUNESRSSCOM_UPDATE_FUTURE.isDone() && !MYTUNESRSSCOM_UPDATE_FUTURE.isCancelled()) {
            MYTUNESRSSCOM_UPDATE_FUTURE.cancel(true);
        }
    }

    public synchronized void scheduleExternalAddressUpdate() {
        GENERAL_EXECUTOR.scheduleWithFixedDelay(new FetchExternalAddressRunnable(), 0, 60, TimeUnit.SECONDS);
    }

    public synchronized void scheduleUpdateCheck() {
        GENERAL_EXECUTOR.scheduleWithFixedDelay(new CheckUpdateRunnable(), 0, 3600, TimeUnit.SECONDS);
    }

    public synchronized void schedule(Runnable runnable, int delay, TimeUnit timeUnit) {
        GENERAL_EXECUTOR.schedule(runnable, delay, timeUnit);
    }

    public synchronized <T> ScheduledFuture<T> schedule(Callable<T> callable, int delay, TimeUnit timeUnit) {
        return GENERAL_EXECUTOR.schedule(callable, delay, timeUnit);
    }

    public synchronized void scheduleWithFixedDelay(Runnable runnable, int initialDelay, int delay, TimeUnit timeUnit) {
        GENERAL_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }
}
