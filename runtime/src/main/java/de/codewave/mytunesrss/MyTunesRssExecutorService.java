/*
 * MyTunesRssExecutorService.java 19.04.2010
 * 
 * Copyright (c) 2010 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package de.codewave.mytunesrss;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.mytunesrss.task.RefreshSmartPlaylistsAndLuceneIndexCallable;

public class MyTunesRssExecutorService {

    private static final ExecutorService DATABASE_UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();

    private static final ExecutorService LUCENE_UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();

    private static Future<Boolean> DATABASE_UPDATE_FUTURE;

    public static synchronized void scheduleDatabaseUpdate() {
        cancelDatabaseUpdate();
        DATABASE_UPDATE_FUTURE = DATABASE_UPDATE_EXECUTOR.submit(new DatabaseBuilderCallable());
    }

    public static synchronized void cancelDatabaseUpdate() {
        if (DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone() && !DATABASE_UPDATE_FUTURE.isCancelled()) {
            DATABASE_UPDATE_FUTURE.cancel(true);
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
}
