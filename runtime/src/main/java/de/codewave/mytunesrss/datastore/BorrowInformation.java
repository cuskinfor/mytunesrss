/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore;

import java.util.Date;
import java.util.UUID;

public class BorrowInformation {
    private long myTimestamp;
    private Throwable myThreadInfo;
    private UUID myId;
    private String myObjectInfo;

    public BorrowInformation(String objectInfo, Throwable threadInfo) {
        myId = UUID.randomUUID();
        myTimestamp = System.currentTimeMillis();
        myObjectInfo = objectInfo;
        myThreadInfo = threadInfo;
    }

    public long getTimestamp() {
        return myTimestamp;
    }

    public UUID getId() {
        return myId;
    }

    public String getObjectInfo() {
        return myObjectInfo;
    }

    public long getAgeMillis() {
        return System.currentTimeMillis() - myTimestamp;
    }

    public Throwable getThreadInfo() {
        return myThreadInfo;
    }

    @Override
    public String toString() {
        return myId + " / " + myObjectInfo + " @ " + new Date(myTimestamp);
    }
}
