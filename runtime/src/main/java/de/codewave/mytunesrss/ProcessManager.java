package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRss.class);

    private Queue<Process> myProcesses = new ConcurrentLinkedQueue<Process>();
    private Thread myEvictorThread;
    private long myEvictionSleepMillis;

    public ProcessManager(long evictionSleepMillis) {
        myEvictionSleepMillis = evictionSleepMillis;
    }

    public synchronized void init() {
        LOGGER.info("Initializing process manager.");
        myEvictorThread = new Thread(this);
        myEvictorThread.start();
    }

    public synchronized void destroy() throws InterruptedException {
        LOGGER.info("Destroying process manager.");
        myEvictorThread.interrupt();
        myEvictorThread.join();
        LOGGER.info("Killing " + myProcesses.size() + " processes in manager queue.");
        for (Process process = myProcesses.poll(); process != null; process = myProcesses.poll()) {
            process.destroy();
        }
    }

    public synchronized void addProcess(Process process) {
        LOGGER.debug("Adding process to manager.");
        myProcesses.offer(process);
    }

    public synchronized void run() {
        try {
            while (true) {
                Process process = myProcesses.poll();
                if (process != null) {
                    try {
                        process.exitValue();
                        LOGGER.debug("Removed finished process from manager.");
                    } catch (IllegalStateException e) {
                        // process is still executing, so re-add it to the queue
                        myProcesses.offer(process);
                    }
                }
                // sleep a while
                Thread.sleep(myEvictionSleepMillis);
            }
        } catch (InterruptedException e) {
            // thread was interrupted, so we let it die
            LOGGER.info("Process manager evictor thread interrupted.");
        }
    }
}
