/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.atomic.AtomicLong;

public class IndexedLoggingEvent {

    private static final AtomicLong INDEX_SEQUENCE = new AtomicLong();

    private long myIndex;

    private LoggingEvent myLoggingEvent;

    public IndexedLoggingEvent(LoggingEvent loggingEvent) {
        myLoggingEvent = loggingEvent;
        myIndex = INDEX_SEQUENCE.getAndIncrement();
    }

    public long getIndex() {
        return myIndex;
    }

    public LoggingEvent getLoggingEvent() {
        return myLoggingEvent;
    }
}
