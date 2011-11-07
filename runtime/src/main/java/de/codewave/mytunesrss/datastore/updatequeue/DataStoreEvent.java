/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

public abstract class DataStoreEvent implements DatabaseUpdateEvent {

    public boolean isTerminate() {
        return false;
    }

    public boolean isStartTransaction() {
        return true;
    }
}
