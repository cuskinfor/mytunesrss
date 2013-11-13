/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.cache;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileSystemCache implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemCache.class);

    private String myName;
    private File myBaseDir;
    private long myMaxSizeBytes;
    private long myIntervalMillis;
    private Thread myThread;

    public FileSystemCache(String name, File baseDir, long maxSizeBytes, long intervalMillis) {
        myName = name;
        myBaseDir = baseDir;
        myMaxSizeBytes = maxSizeBytes;
        myIntervalMillis = intervalMillis;
    }

    public void init() throws IOException {
        if (!myBaseDir.isDirectory()) {
            if (!myBaseDir.mkdirs()) {
                throw new IOException("[Cache: " + myName + "] Could not create cache dir \"" + myBaseDir + "\".");
            }
        }
        myThread = new Thread(this, "CacheWorker-" + myName);
        myThread.start();
    }

    public boolean clear() {
        boolean result = true;
        for (File file : myBaseDir.listFiles()) {
            if (file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    result = false;
                }
            } else {
                result &= file.delete();
            }
        }
        return result;
    }

    public void destroy() {
        myThread.interrupt();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            LOGGER.warn("[Cache: \"" + myName + "\"] Interrupted while waiting for cache thread to die.", e);
        }
    }

    public synchronized void setMaxSizeBytes(long maxSizeBytes) {
        myMaxSizeBytes = maxSizeBytes;
    }

    public synchronized long deleteByPrefix(String prefix) {
        long count = 0;
        for (CacheItem item : listItems()) {
            if (item.getId().startsWith(prefix)) {
                if (deleteByName(item.getId())) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized long deleteBySuffix(String suffix) {
        long count = 0;
        for (CacheItem item : listItems()) {
            if (item.getId().endsWith(suffix)) {
                if (deleteByName(item.getId())) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized void truncateCache() {
        LOGGER.debug("[Cache: \"" + myName + "\"] Truncating cache.");
        long startTime = System.currentTimeMillis();
        List<CacheItem> items = listItems();
        Collections.sort(items, new Comparator<CacheItem>() {
            public int compare(CacheItem item1, CacheItem item2) {
                long diff = item1.getLastAccessTime() - item2.getLastAccessTime();
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                }
                return 0;
            }
        });
        long size = 0;
        for (CacheItem item : items) {
            size += item.getSize();
        }
        LOGGER.debug("[Cache: \"" + myName + "\"] Found " + items.size() + " items with a total size of " + size + " bytes (" + myMaxSizeBytes + " bytes allowed).");
        if (size > myMaxSizeBytes) {
            for (CacheItem item : items) {
                long itemSize = item.getSize();
                if (deleteByName(item.getId())) {
                    LOGGER.debug("[Cache: \"" + myName + "\"] Cache item \"" + item.getId() + " deleted.");
                    size -= itemSize;
                    if (size < myMaxSizeBytes) {
                        break; // done deleting old files
                    }
                }
            }
        }
        LOGGER.debug("[Cache: \"" + myName + "\"] Done truncating cache in " + (System.currentTimeMillis() - startTime) + " millis.");
    }

    public synchronized boolean deleteByName(String name) {
        File file = new File(myBaseDir, name);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(new File(myBaseDir, name));
            } catch (IOException e) {
                return false;
            }
            return true;
        } else if (file.isFile()) {
            return new File(myBaseDir, name).delete();
        } else {
            throw new IllegalArgumentException("[Cache: \"" + myName + "\"] No item with name \"" + name + "\" found in cache.");
        }
    }

    private List<CacheItem> listItems() {
        List<CacheItem> items = new ArrayList<CacheItem>();
        for (File file : myBaseDir.listFiles()) {
            if (file.isDirectory()) {
                items.add(new CacheItem(file.getName(), getDirSize(file), getDirLastModified(file)));
            } else {
                items.add(new CacheItem(file.getName(), file.length(), file.lastModified()));
            }
        }
        return items;
    }

    private long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            size += file.length();
        }
        return size;
    }

    private long getDirLastModified(File dir) {
        long lastModified = dir.lastModified();
        for (File file : dir.listFiles()) {
            lastModified = Math.max(lastModified, file.lastModified());
        }
        return lastModified;
    }

    public File getBaseDir() {
        return myBaseDir;
    }

    public void touch(String name) {
        File file = new File(myBaseDir, name);
        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis());
        }
    }

    public File createTempFile() throws IOException {
        return File.createTempFile("mytunesrss_", ".tmp", getBaseDir());
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                truncateCache();
                Thread.sleep(myIntervalMillis);
            } catch (InterruptedException e) {
                // this is the expected way to end the thread
                return;
            }
        }
    }
}
