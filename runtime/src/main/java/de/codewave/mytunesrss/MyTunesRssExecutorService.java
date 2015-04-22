/*
 * MyTunesRssExecutorService.java 19.04.2010
 *
 * Copyright (c) 2010 1&1 Internet AG. All rights reserved.
 *
 * $Id$
 */
package de.codewave.mytunesrss;

import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.mytunesrss.task.BackupDatabaseRunnable;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.mytunesrss.task.DatabaseMaintenanceRunnable;
import de.codewave.mytunesrss.task.RecreateDatabaseRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyTunesRssExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssExecutorService.class);

    private final ExecutorService DATABASE_JOB_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ExecutorService LUCENE_UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ScheduledExecutorService GENERAL_EXECUTOR = Executors.newScheduledThreadPool(20);

    private ExecutorService ON_DEMAND_THUMBNAIL_GENERATOR = Executors.newFixedThreadPool(5);

    private Future<Boolean> DATABASE_UPDATE_FUTURE;

    private Future DATABASE_RESET_FUTURE;

    private Future DATABASE_BACKUP_FUTURE;

    private Future DATABASE_MAINTENANCE_FUTURE;

    private ScheduledFuture PHOTO_THUMBNAIL_GENERATOR_FUTURE;

    private PhotoThumbnailGeneratorRunnable myPhotoThumbnailGeneratorRunnable;

    private ScheduledFuture TRACK_IMAGE_GENERATOR_FUTURE;

    private TrackImageGeneratorRunnable myTrackImageGeneratorRunnable;
    
    public AtomicBoolean PLAY_COUNT_UPDATED = new AtomicBoolean(false);
    
    public volatile long LAST_SCHEDULED_PLAYLIST_UPDATE;

    public synchronized void shutdown() throws InterruptedException {
        for (final ExecutorService executorService : new ExecutorService[] {DATABASE_JOB_EXECUTOR, LUCENE_UPDATE_EXECUTOR, GENERAL_EXECUTOR, ON_DEMAND_THUMBNAIL_GENERATOR}) {
            executorService.shutdown();
        }
    }

    public synchronized void shutdownNow() throws InterruptedException {
        Collection<Thread> threads = new ArrayList<>();
        for (final ExecutorService executorService : new ExecutorService[] {DATABASE_JOB_EXECUTOR, LUCENE_UPDATE_EXECUTOR, GENERAL_EXECUTOR, ON_DEMAND_THUMBNAIL_GENERATOR}) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    executorService.shutdownNow();
                    try {
                        executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        LOGGER.warn("Interrupted while waiting for termination of executor service.", e);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join(10000);
        }
    }

    public synchronized void scheduleDatabaseUpdate(Collection<DatasourceConfig> dataSources, boolean ignoreTimestamps) throws DatabaseJobRunningException {
        if (isDatabaseJobRunning()) {
            throw new DatabaseJobRunningException();
        }
        try {
            DATABASE_UPDATE_FUTURE = DATABASE_JOB_EXECUTOR.submit(new DatabaseBuilderCallable(dataSources, ignoreTimestamps));
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule database update task.", e);
        }
    }

    public synchronized void cancelDatabaseUpdate() {
        if (DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone()) {
            DATABASE_UPDATE_FUTURE.cancel(true);
        }
    }

    public synchronized void scheduleDatabaseReset() throws DatabaseJobRunningException {
        if (isDatabaseJobRunning()) {
            throw new DatabaseJobRunningException();
        }
        try {
            DATABASE_RESET_FUTURE = DATABASE_JOB_EXECUTOR.submit(new RecreateDatabaseRunnable());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule database reset task.", e);
        }
    }

    public synchronized void scheduleDatabaseBackup() {
        cancelDatabaseBackupJob();
        try {
            DATABASE_BACKUP_FUTURE = DATABASE_JOB_EXECUTOR.submit(new BackupDatabaseRunnable());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule database backup task.", e);
        }
    }

    public synchronized void scheduleDatabaseMaintenance() {
        try {
            DATABASE_MAINTENANCE_FUTURE = DATABASE_JOB_EXECUTOR.submit(new DatabaseMaintenanceRunnable());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule database maintenance task.", e);
        }
    }

    public synchronized <T> Future<T> scheduleDatabaseJob(Callable<T> job) {
        return DATABASE_JOB_EXECUTOR.submit(job);
    }

    public synchronized boolean isDatabaseJobRunning() {
        if (DATABASE_UPDATE_FUTURE != null && !DATABASE_UPDATE_FUTURE.isDone()) {
            return true;
        }
        return DATABASE_RESET_FUTURE != null && !DATABASE_RESET_FUTURE.isDone();
    }

    public synchronized void cancelDatabaseBackupJob() {
        if (DATABASE_BACKUP_FUTURE != null && !DATABASE_BACKUP_FUTURE.isDone()) {
            DATABASE_BACKUP_FUTURE.cancel(true);
        }
    }

    public synchronized void cancelDatabaseMaintenanceJob() {
        if (DATABASE_MAINTENANCE_FUTURE != null && !DATABASE_MAINTENANCE_FUTURE.isDone()) {
            DATABASE_MAINTENANCE_FUTURE.cancel(true);
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

    public synchronized boolean isDatabaseMaintenanceRunning() {
        return DATABASE_MAINTENANCE_FUTURE != null && !DATABASE_MAINTENANCE_FUTURE.isDone();
    }

    public synchronized void scheduleImageGenerators() {
        cancelImageGenerators();
        myPhotoThumbnailGeneratorRunnable = new PhotoThumbnailGeneratorRunnable();
        PHOTO_THUMBNAIL_GENERATOR_FUTURE = scheduleWithFixedDelay(myPhotoThumbnailGeneratorRunnable, 0, 60, TimeUnit.SECONDS);
        myTrackImageGeneratorRunnable = new TrackImageGeneratorRunnable();
        TRACK_IMAGE_GENERATOR_FUTURE = scheduleWithFixedDelay(myTrackImageGeneratorRunnable, 0, 60, TimeUnit.SECONDS);
    }

    public synchronized void cancelImageGenerators() {
        if (myPhotoThumbnailGeneratorRunnable != null && PHOTO_THUMBNAIL_GENERATOR_FUTURE != null) {
            PHOTO_THUMBNAIL_GENERATOR_FUTURE.cancel(true);
            myPhotoThumbnailGeneratorRunnable.waitForTermination();
            myPhotoThumbnailGeneratorRunnable = null;
        }
        if (myTrackImageGeneratorRunnable != null && TRACK_IMAGE_GENERATOR_FUTURE != null) {
            TRACK_IMAGE_GENERATOR_FUTURE.cancel(true);
            myTrackImageGeneratorRunnable.waitForTermination();
            myTrackImageGeneratorRunnable = null;
        }
    }

    public synchronized void scheduleExternalAddressUpdate() {
        try {
            scheduleWithFixedDelay(new FetchExternalAddressRunnable(), 0, 300, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule external address update task.", e);
        }
    }

    public synchronized void scheduleSmartPlaylistRefresh() {
        try {
            scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() - LAST_SCHEDULED_PLAYLIST_UPDATE > 3600000) {
                        LAST_SCHEDULED_PLAYLIST_UPDATE = System.currentTimeMillis();
                        // Approximately once an hour run an update for update/play time related smart playlists
                        StopWatch.start("Running scheduled refresh of update/play time related smart playlists.");
                        try {
                            MyTunesRss.STORE.executeStatement(new RefreshSmartPlaylistsStatement(RefreshSmartPlaylistsStatement.UpdateType.SCHEDULED));
                        } catch (SQLException | RuntimeException ignored) {
                            LOGGER.warn("Could not update smart playlists.");
                        } finally {
                            StopWatch.stop();
                        }
                    } else if (PLAY_COUNT_UPDATED.getAndSet(false)) {
                        // if the play count of any track was updated since the last refresh, run an update for play count related smart playlists
                        StopWatch.start("Running scheduled refresh of play count related smart playlists.");
                        try {
                            MyTunesRss.STORE.executeStatement(new RefreshSmartPlaylistsStatement(RefreshSmartPlaylistsStatement.UpdateType.ON_PLAY));
                        } catch (SQLException | RuntimeException ignored) {
                            LOGGER.warn("Could not update smart playlists.");
                        } finally {
                            StopWatch.stop();
                        }
                    }
                }
            }, 15, 15, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule smart playlist refresh task.", e);
        }
    }

    public synchronized void schedule(Runnable runnable, int delay, TimeUnit timeUnit) {
        try {
            GENERAL_EXECUTOR.schedule(runnable, delay, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
    }

    public synchronized void execute(Runnable runnable) {
        try {
            GENERAL_EXECUTOR.execute(runnable);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
    }

    public synchronized <T> Future<T> submit(Callable<T> callable) {
        try {
            return GENERAL_EXECUTOR.submit(callable);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
        return null;
    }

    public synchronized <T> ScheduledFuture<T> schedule(Callable<T> callable, int delay, TimeUnit timeUnit) {
        try {
            return GENERAL_EXECUTOR.schedule(callable, delay, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
        return null;
    }

    public synchronized ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, int initialDelay, int delay, TimeUnit timeUnit) {
        try {
            return GENERAL_EXECUTOR.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
        return null;
    }

    public synchronized ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, int initialDelay, int period, TimeUnit timeUnit) {
        try {
            return GENERAL_EXECUTOR.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Could not schedule task.", e);
        }
        return null;
    }

    public synchronized void setOnDemandThumbnailGeneratorThreads() {
        if (ON_DEMAND_THUMBNAIL_GENERATOR != null) {
            ON_DEMAND_THUMBNAIL_GENERATOR.shutdown();
        }
        ON_DEMAND_THUMBNAIL_GENERATOR = Executors.newFixedThreadPool(MyTunesRss.CONFIG.getOnDemandThumbnailGenerationThreads());
    }

    public synchronized Future<String> generatePhotoThumbnail(String photoId, File photoFile) {
        return ON_DEMAND_THUMBNAIL_GENERATOR.submit(new OnDemandPhotoThumbnailGeneratorCallable(photoId, photoFile));
    }
}
