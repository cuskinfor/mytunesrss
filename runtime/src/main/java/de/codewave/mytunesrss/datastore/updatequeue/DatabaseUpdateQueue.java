package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseUpdateQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUpdateQueue.class);

    private BlockingQueue<DatabaseUpdateEvent> myQueue = new SynchronousQueue<DatabaseUpdateEvent>();

    private AtomicBoolean myTerminated = new AtomicBoolean(false);

    public DatabaseUpdateQueue(final long maxTxDurationMillis) {
        new Thread(new Runnable() {
            public synchronized void run() {
                long txBegin = System.currentTimeMillis();
                long checkpointStartTime = 0;
                DataStoreSession tx = null;
                try {
                    DatabaseUpdateEvent event;
                    do {
                        if (checkpointStartTime > 0 && System.currentTimeMillis() - checkpointStartTime > 60000) { // 1 minute
                            LOGGER.debug("Checkpoint reached.");
                            event = new CheckpointEvent();
                            checkpointStartTime = 0;
                        } else {
                            long pollTimeoutMillis = Math.max(0, tx != null ? maxTxDurationMillis - (System.currentTimeMillis() - txBegin) : Long.MAX_VALUE);
                            LOGGER.debug("Polling queue with a timeout of " + pollTimeoutMillis + " ms.");
                            event = myQueue.poll(pollTimeoutMillis, TimeUnit.MILLISECONDS);
                        }
                        LOGGER.debug("Received " + event);
                        if (event != null) {
                            if (tx == null && event.isStartTransaction()) {
                                LOGGER.debug("Starting new transaction.");
                                tx = MyTunesRss.STORE.getTransaction();
                                txBegin = System.currentTimeMillis();
                            }
                            if (!event.execute(tx)) {
                                tx = null;
                            }
                        }
                        if (tx != null) {
                            long txDurationMillis = System.currentTimeMillis() - txBegin;
                            if (txDurationMillis > maxTxDurationMillis || event == null) {
                                LOGGER.debug("Committing transaction after " + txDurationMillis + " ms.");
                                tx.commit();
                                tx = null;
                            }
                        }
                        if (event != null && event.isCheckpointRelevant() && checkpointStartTime == 0) {
                            LOGGER.debug("Setting checkpoint start time after checkpoint relevant event.");
                            checkpointStartTime = System.currentTimeMillis();
                        }
                    } while ((event == null || !event.isTerminate()) && !Thread.interrupted());
                } catch (InterruptedException e) {
                    LOGGER.info("Interrupted while waiting for event.");
                } finally {
                    if (tx != null) {
                        tx.commit();
                    }
                    myTerminated.set(true);
                    notifyAll();
                }
                LOGGER.info("Terminating database update queue thread.");
            }
        }, "DatabaseUpdateQueueWorker").start();
    }

    public void offer(DatabaseUpdateEvent event) throws InterruptedException {
        if (!myQueue.offer(event, 600000, TimeUnit.MILLISECONDS)) {
            LOGGER.error("Lost database update event \"" + event + "\".");
        }
    }

    public synchronized void waitForTermination() {
        try {
            while (!myTerminated.get()) {
                wait();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for queue termination.", e);
        }
    }
}
