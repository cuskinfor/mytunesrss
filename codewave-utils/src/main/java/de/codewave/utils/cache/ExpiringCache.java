package de.codewave.utils.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class for caching file references. Adding a file will add a reference to the file. When the timeout is reached, the file will be deleted from the
 * cache and the file system. The timeout is reset whenever a file from the cache is touched or retrieved.
 */
public class ExpiringCache<T extends ExpiringCacheItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiringCache.class);
    private Map<String, T> myItems = new HashMap<String, T>();
    private Timer myTimer;
    private int myMaxItems;

    public ExpiringCache(String name, int cleanupInterval, int maxItems) {
        myTimer = new Timer("itemCache[" + name + "]-cleanupTimer", true);
        myMaxItems = maxItems;
        myTimer.schedule(new CleanupTask(), cleanupInterval, cleanupInterval);
    }

    public synchronized void add(T item) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding item with identifier \"" + item.myIdentifier + "\" and timeout \"" + item.myTimeout + "\" to cache.");
        }
        myItems.put(item.myIdentifier, item);
        item.lock();// prevent from being removed
        truncate();
        item.unlock();
    }

    public synchronized boolean putIfAbsent(T item) {
        if (!myItems.containsKey(item.getIdentifier())) {
            add(item);
            return true;
        }
        return false;
    }

    public synchronized void truncate(int maxFiles) {
        myMaxItems = maxFiles;
        truncate();
    }

    public synchronized void truncate() {
        if (myItems.size() > myMaxItems) {
            List<T> sortedInfos = getSorted();
            for (Iterator<T> iterator = sortedInfos.iterator(); myItems.size() > myMaxItems && iterator.hasNext();) {
                T info = iterator.next();
                if (info.expire()) {
                    myItems.remove(info.getIdentifier());
                }
            }
        }
    }

    private synchronized List<T> getSorted() {
        List<T> sortedItems = new ArrayList<T>(myItems.values());
        Collections.sort(sortedItems, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return (int) (o1.myExpiration - o2.myExpiration);
            }
        });
        return sortedItems;
    }

    public synchronized void unlockAll() {
        for (T item : myItems.values()) {
            item.myLockCount = 0;
        }
    }

    public synchronized void touch(String identifier) {
        T info = myItems.get(identifier);
        if (info != null) {
            info.touch();
        }
    }

    public synchronized T lock(String identifier) {
        T info = myItems.get(identifier);
        if (info != null) {
            info.lock();
        }
        return get(identifier);
    }

    public synchronized T unlock(String identifier) {
        T info = myItems.get(identifier);
        if (info != null) {
            info.unlock();
        }
        return get(identifier);
    }

    public synchronized T get(String identifier) {
        T item = myItems.get(identifier);
        if (item != null) {
            item.touch();
            return item;
        }
        return null;
    }

    protected synchronized T untouchedGet(String identifier) {
        T item = myItems.get(identifier);
        if (item != null) {
            return item;
        }
        return null;
    }

    public Set<String> keySet() {
        return new HashSet<String>(myItems.keySet());
    }

    public synchronized void remove(String identifier) {
        T item = myItems.remove(identifier);
        if (item != null) {
            item.expire();
        }
    }

    public synchronized void clearCache() {
        for (Map.Entry<String, T> entry : myItems.entrySet()) {
            entry.getValue().onItemExpired();
        }
        myItems.clear();
    }

    public class CleanupTask extends TimerTask {
        public void run() {
            try {
                synchronized (ExpiringCache.this) {
                    for (Iterator<Map.Entry<String, T>> iter = myItems.entrySet().iterator(); iter.hasNext();) {
                        T item = iter.next().getValue();
                        if (item.isExpired()) {
                            if (item.expire()) {
                                iter.remove();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("ExpiringCacheCleanupTask exited with error.", e);
            }
        }
    }
}