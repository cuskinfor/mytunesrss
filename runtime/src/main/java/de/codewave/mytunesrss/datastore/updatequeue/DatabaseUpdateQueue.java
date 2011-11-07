package de.codewave.mytunesrss.datastore.updatequeue;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseUpdateQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUpdateQueue.class);

    private BlockingQueue<DatabaseUpdateEvent> myQueue = new LinkedBlockingQueue<DatabaseUpdateEvent>();

    public DatabaseUpdateQueue(final long maxTxDurationMillis) {
        new Thread(new Runnable() {
            public void run() {
                long txBegin = System.currentTimeMillis();
                DataStoreSession tx = null;
                boolean terminate = false;
                while (!terminate) {
                    long pollTimeoutMillis = Math.max(0, tx != null ? maxTxDurationMillis - (System.currentTimeMillis() - txBegin) : Long.MAX_VALUE);
                    try {
                        LOGGER.debug("Polling queue with a timeout of "  + pollTimeoutMillis + " ms.");
                        DatabaseUpdateEvent event = myQueue.poll(pollTimeoutMillis, TimeUnit.MILLISECONDS);
                        if (event != null) {
                            LOGGER.debug("Received \"" + event.getClass().getName() + "\" event.");
                            if (event instanceof TerminateEvent) {
                                terminate = true;
                            } else if (event instanceof TransactionalEvent) {
                                if (tx == null && !((TransactionalEvent)event).isIgnoreWithoutTransaction()) {
                                    LOGGER.debug("Starting new transaction.");
                                    tx = MyTunesRss.STORE.getTransaction();
                                    txBegin = System.currentTimeMillis();
                                }
                            }
                            if (event instanceof TransactionalEvent && tx != null) {
                                tx = ((TransactionalEvent)event).execute(tx);
                            } else if (event instanceof NonTransactionalEvent) {
                                ((NonTransactionalEvent)event).execute();
                            }
                        }
                        if (tx != null) {
                            long txDurationMillis = System.currentTimeMillis() - txBegin;
                            if (txDurationMillis > maxTxDurationMillis) {
                                LOGGER.debug("Committing transaction after " + txDurationMillis + " ms.");
                                tx.commit();
                                tx = null;
                            }
                        }
                    } catch (InterruptedException e) {
                        LOGGER.info("Interrupted while waiting for event.");
                        terminate = true;
                    }
                }
                LOGGER.info("Terminating database update queue thread.");
            }
        }).start();
    }

    public void offer(DatabaseUpdateEvent event) {
        myQueue.offer(event);
    }
}
