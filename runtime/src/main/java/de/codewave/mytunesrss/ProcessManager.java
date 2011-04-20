package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessManager.class);

    private Queue<Process> myProcesses = new ConcurrentLinkedQueue<Process>();
    private Thread myEvictorThread;
    private long myEvictionSleepMillis;

    public ProcessManager(long evictionSleepMillis) {
        myEvictionSleepMillis = evictionSleepMillis;
    }

    public void init() {
        LOGGER.info("Initializing process manager.");
        myEvictorThread = new Thread(this);
        myEvictorThread.setDaemon(true);
        myEvictorThread.start();
    }

    public void destroy() throws InterruptedException {
        LOGGER.info("Destroying process manager.");
        if (myEvictorThread != null) {
            myEvictorThread.interrupt();
            myEvictorThread.join();
        }
        synchronized (myProcesses) {
            LOGGER.info("Killing " + myProcesses.size() + " processes in manager queue.");
            for (Process process = myProcesses.poll(); process != null; process = myProcesses.poll()) {
                process.destroy();
            }
        }
    }

    public void addProcess(Process process) {
        LOGGER.debug("Adding process to manager.");
        synchronized (myProcesses) {
            myProcesses.offer(process);
        }
    }

    public void run() {
        try {
            while (true) {
                synchronized (myProcesses) {
                    Process process = myProcesses.poll();
                    if (process != null) {
                        try {
                            process.exitValue();
                            LOGGER.debug("Removed finished process from manager.");
                        } catch (IllegalThreadStateException e) {
                            // process is still executing, so re-add it to the queue
                            myProcesses.offer(process);
                        }
                    }

                }
                // sleep a while
                Thread.sleep(myEvictionSleepMillis);
            }
        } catch (InterruptedException
                e) {
            // thread was interrupted, so we let it die
            LOGGER.info("Process manager evictor thread interrupted.");
        }
    }
}
