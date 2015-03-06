package de.codewave.mytunesrss;

import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LogQueueManager {
    private static final int BACKLOG_SIZE = 1000;
    private static final int QUEUE_SIZE = BACKLOG_SIZE * 2;
    private Queue<LoggingEvent> myBacklog = new ArrayDeque<>(BACKLOG_SIZE);
    private Set<Queue<LoggingEvent>> myQueues = new HashSet<>();

    public synchronized void offer(LoggingEvent event) {
        if (myBacklog.size() == BACKLOG_SIZE) {
            myBacklog.poll();
        }
        myBacklog.offer(event);
        for (Queue<LoggingEvent> queue : myQueues) {
            queue.offer(event);
        }
    }

    public synchronized void removeQueue(Queue<LoggingEvent> queue) {
        myQueues.remove(queue);
    }

    public synchronized BlockingQueue<LoggingEvent> createQueue() {
        BlockingQueue<LoggingEvent> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        for (LoggingEvent aMyBacklog : myBacklog) {
            queue.offer(aMyBacklog);
        }
        myQueues.add(queue);
        return queue;
    }
}
