package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;

public class DatabaseUpdateQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUpdateQueue.class);

    private BlockingQueue<DatabaseUpdateEvent> myQueue = new SynchronousQueue<DatabaseUpdateEvent>();

    public DatabaseUpdateQueue(final long maxTxDurationMillis) {
        new Thread(new Runnable() {
            public void run() {
                long txBegin = System.currentTimeMillis();
                DataStoreSession tx = null;
                try {
                    DatabaseUpdateEvent event;
                    do {
                        long pollTimeoutMillis = Math.max(0, tx != null ? maxTxDurationMillis - (System.currentTimeMillis() - txBegin) : Long.MAX_VALUE);
                        LOGGER.debug("Polling queue with a timeout of " + pollTimeoutMillis + " ms.");
                        event = myQueue.poll(pollTimeoutMillis, TimeUnit.MILLISECONDS);
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
                    } while (event == null || !event.isTerminate());
                } catch (InterruptedException e) {
                    LOGGER.info("Interrupted while waiting for event.");
                }
                LOGGER.info("Terminating database update queue thread.");
            }
        }, "DatabaseUpdateQueueWorker").start();
    }

    public void offer(DatabaseUpdateEvent event) throws InterruptedException {
        if (!myQueue.offer(event, 60000, TimeUnit.MILLISECONDS)) {
            LOGGER.error("Lost database update event \"" + event + "\".");
        }
    }
}
