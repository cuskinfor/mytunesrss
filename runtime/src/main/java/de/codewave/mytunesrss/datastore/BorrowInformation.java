/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore;

public class BorrowInformation {
    private long myTimestamp;
    private Throwable myThreadInfo;

    public BorrowInformation(Throwable threadInfo) {
        this(System.currentTimeMillis(), threadInfo);
    }

    public BorrowInformation(long timestamp, Throwable threadInfo) {
        myTimestamp = timestamp;
        myThreadInfo = threadInfo;
    }

    public long getTimestamp() {
        return myTimestamp;
    }

    public long getAgeMillis() {
        return System.currentTimeMillis() - myTimestamp;
    }

    public Throwable getThreadInfo() {
        return myThreadInfo;
    }
}
