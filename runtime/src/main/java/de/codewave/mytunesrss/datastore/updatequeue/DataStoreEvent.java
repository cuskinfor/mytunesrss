/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.updatequeue;

public abstract class DataStoreEvent implements DatabaseUpdateEvent {

    @Override
    public boolean isTerminate() {
        return false;
    }

    @Override
    public boolean isStartTransaction() {
        return true;
    }
}
