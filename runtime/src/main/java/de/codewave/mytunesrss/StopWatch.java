package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopWatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopWatch.class);
    
    private static final ThreadLocal<Long> STOP_WATCH_START = new ThreadLocal<Long>();

    private static final ThreadLocal<String> STOP_WATCH_MSG = new ThreadLocal<String>();

    public static void start(String message) {
        STOP_WATCH_START.set(System.currentTimeMillis());
        STOP_WATCH_MSG.set(message);
        LOGGER.info("Start <" + STOP_WATCH_MSG.get() + ">");
    }

    public static void stop() {
        LOGGER.info("Stop <" + STOP_WATCH_MSG.get() + "> (duration = " + (System.currentTimeMillis() - STOP_WATCH_START.get()) + " ms)");
    }
}
