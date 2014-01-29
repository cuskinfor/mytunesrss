package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class StopWatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopWatch.class);

    private static final ThreadLocal<Stack<Long>> STOP_WATCH_START = new ThreadLocal<Stack<Long>>() {
        @Override
        protected Stack<Long> initialValue() {
            return new Stack<>();
        }
    };

    private static final ThreadLocal<Stack<String>> STOP_WATCH_MSG = new ThreadLocal<Stack<String>>() {
        @Override
        protected Stack<String> initialValue() {
            return new Stack<>();
        }
    };

    public static void start(String message) {
        STOP_WATCH_START.get().push(System.currentTimeMillis());
        STOP_WATCH_MSG.get().push(message);
        LOGGER.info("Start <" + message + ">");
    }

    public static void stop() {
        LOGGER.info("Stop <" + STOP_WATCH_MSG.get().pop() + "> (duration = " + (System.currentTimeMillis() - STOP_WATCH_START.get().pop()) + " ms)");
    }
}
