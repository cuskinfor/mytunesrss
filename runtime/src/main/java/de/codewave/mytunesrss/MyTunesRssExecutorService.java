/*
 * MyTunesRssExecutorService.java 19.04.2010
 * 
 * Copyright (c) 2010 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package de.codewave.mytunesrss;

import java.util.concurrent.*;

import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.mytunesrss.task.RecreateDatabaseCallable;
import de.codewave.mytunesrss.task.RefreshSmartPlaylistsAndLuceneIndexCallable;

public class MyTunesRssExecutorService {

    private static final ExecutorService DATABASE_JOB_EXECUTOR = Executors.newSingleThreadExecutor();

    private static final ExecutorService LUCENE_UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();

    private static final ScheduledExecutorService GENERAL_EXECUTOR = Executors.newScheduledThreadPool(10);

    private static Future<Boolean> DATABASE_UPDATE_FUTURE;

    private static Future<Void> DATABASE_RESET_FUTURE;

    private static ScheduledFuture MYTUNESRSSCOM_UPDATE_FUTURE;

    public static synchronized void scheduleDatabaseUpdate() {
        cancelDatabaseJob();
        DATABASE_UPDATE_FUTURE = DATABASE_JOB_EXECUTOR.submit(new DatabaseBuilderCallable());
    }

    public static synchronized void scheduleDatabaseReset() {
        cancelDatabaseJob();
        DATABASE_RESET_FUTURE = DATABASE_JOB_EXECUTOR.submit(new RecreateDatabaseCallable());
    }

    public static synchronized void cancelDatabaseJob() {
        if (DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone() && !DATABASE_UPDATE_FUTURE.isCancelled()) {
            DATABASE_UPDATE_FUTURE.cancel(true);
        }
        if (DATABASE_RESET_FUTURE != null && !DATABASE_RESET_FUTURE.isDone() && !DATABASE_RESET_FUTURE.isCancelled()) {
            DATABASE_RESET_FUTURE.cancel(true);
        }
    }

    public static synchronized boolean isDatabaseUpdateRunning() {
        return DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone()
                && !DATABASE_UPDATE_FUTURE.isCancelled();
    }

    public static synchronized boolean getDatabaseUpdateResult() throws InterruptedException, ExecutionException {
        return DATABASE_UPDATE_FUTURE != null ? DATABASE_UPDATE_FUTURE.get() : false;
    }

    public static synchronized void scheduleLuceneAndSmartPlaylistUpdate(String[] trackIds) {
        LUCENE_UPDATE_EXECUTOR.submit(new RefreshSmartPlaylistsAndLuceneIndexCallable(trackIds));
    }

    public static synchronized void scheduleMyTunesRssComUpdate() {
        MYTUNESRSSCOM_UPDATE_FUTURE = GENERAL_EXECUTOR.scheduleWithFixedDelay(new MyTunesRssComUpdateRunnable(), 0, 5, TimeUnit.MINUTES);
    }

    public static synchronized void cancelMyTunesRssComUpdate() {
        if (MYTUNESRSSCOM_UPDATE_FUTURE != null && !MYTUNESRSSCOM_UPDATE_FUTURE.isDone() && !MYTUNESRSSCOM_UPDATE_FUTURE.isCancelled()) {
            MYTUNESRSSCOM_UPDATE_FUTURE.cancel(true);
        }
    }

    public static synchronized void scheduleExternalAddressUpdate() {
        GENERAL_EXECUTOR.scheduleWithFixedDelay(new FetchExternalAddressRunnable(), 0, 1, TimeUnit.MINUTES);
    }

    public static synchronized void scheduleUpdateCheck() {
        GENERAL_EXECUTOR.scheduleWithFixedDelay(new CheckUpdateRunnable(), 0, 1, TimeUnit.HOURS);
    }

    public static synchronized void schedule(Runnable runnable, int delay, TimeUnit timeUnit) {
        GENERAL_EXECUTOR.schedule(runnable, delay, timeUnit);
    }
}
