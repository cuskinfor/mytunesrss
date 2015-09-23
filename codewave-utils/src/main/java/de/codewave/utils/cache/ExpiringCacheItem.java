package de.codewave.utils.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExpiringCacheItem {
    private static final Logger LOG = LoggerFactory.getLogger(ExpiringCacheItem.class);

    String myIdentifier;
    long myTimeout;
    long myExpiration;
    int myLockCount;

    protected ExpiringCacheItem(String identifier, long timeout) {
        myIdentifier = identifier;
        myTimeout = timeout;
        myExpiration = System.currentTimeMillis() + timeout;
    }

    boolean expire() {
        if (!isLocked()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Item with identifier \"" + myIdentifier + "\" is expired.");
            }
            onItemExpired();
            return true;
        }
        return false;
    }

    void lock() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Locking item with identifier \"" + myIdentifier + "\".");
        }
        myLockCount++;
    }

    void unlock() {
        if (myLockCount == 0) {
            throw new RuntimeException(
                    "Cannot unlock item idenfifier \"" + myIdentifier + "\" because it is not locked.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unlocking item with identifier \"" + myIdentifier + "\".");
        }
        myLockCount--;
    }

    boolean isLocked() {
        return myLockCount > 0;
    }

    void touch() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Touching item with identifier \"" + myIdentifier + "\".");
        }
        myExpiration = System.currentTimeMillis() + myTimeout;
    }

    String getIdentifier() {
        return myIdentifier;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > myExpiration;
    }

    protected abstract void onItemExpired();

}
